package impl

import (
	"context"
	"database/sql"
	"encoding/json"
	"errors"
	"fmt"
	"log"
	"strconv"
	"strings"
	"time"

	"example.com/be/global"
	"example.com/be/internal/consts"
	"example.com/be/internal/database"
	"example.com/be/internal/model"
	"example.com/be/internal/service"
	"example.com/be/internal/utils"
	"example.com/be/internal/utils/auth"
	"example.com/be/internal/utils/crypto"
	"example.com/be/internal/utils/random"
	"example.com/be/internal/utils/sendto"
	"example.com/be/response"
	"github.com/google/uuid"
	"github.com/redis/go-redis/v9"
)

// struct
type sUserLogin struct {
	r *database.Queries
}

// new sUserLogin implementation interface for IUserLogin
func NewSUserLogin(r *database.Queries) service.IUserLogin {
	return &sUserLogin{
		r: r,
	}
}

// Login implements service.IUserLogin.
func (s *sUserLogin) Login(ctx context.Context, in *model.LoginInput) (codeResult int, out model.LoginOutput, err error) {
	// check user in table user_base
	userBase, err := s.r.GetOneUserInfo(ctx, in.UserAccount)
	if err != nil {
		return response.ErrCodeAuthFailed, out, err
	}
	// check password
	if !crypto.ComparePasswordWithHash(in.UserPassword, userBase.UserSalt, userBase.UserPassword) {
		return response.ErrCodeAuthFailed, out, errors.New("password not match")
	}

	// upgrade state login
	go s.r.LoginUserBase(ctx, database.LoginUserBaseParams{
		UserLoginIp:  sql.NullString{String: "127.0.0.1", Valid: true}, // TODO: get ip device login
		UserAccount:  in.UserAccount,
		UserPassword: userBase.UserPassword,
	})
	// create uuid
	subToken := utils.GenerateCliTokenUUID(userBase.UserID)
	log.Println("subToken: ", subToken)
	// get user info table
	infoUser, err := s.r.GetUserWithID(ctx, userBase.UserID)
	if err != nil {
		return response.ErrCodeAuthFailed, out, err
	}
	// convert to json
	infoUserJson, err := json.Marshal(infoUser)
	if err != nil {
		return response.ErrCodeAuthFailed, out, fmt.Errorf("convert json failed: %w", err)
	}
	// give infoUserJson to redis with key = subToken
	err = global.Rdb.Set(ctx, subToken, infoUserJson, time.Duration(consts.TIME_2FA_OTP_REGISTER)*time.Hour).Err()
	if err != nil {
		return response.ErrCodeAuthFailed, out, fmt.Errorf("set redis failed: %w", err)
	}
	// create token
	out.Token, err = auth.CreateToken(subToken)
	if err != nil {
		return response.ErrCodeAuthFailed, out, fmt.Errorf("create access token failed: %w", err)
	}
	// create refresh token
	uuidRefreshToken := uuid.New().String()
	out.RefreshToken, err = auth.CreateRefreshToken(uuidRefreshToken)
	if err != nil {
		return response.ErrCodeAuthFailed, out, fmt.Errorf("create refresh token failed: %w", err)
	}
	// save access token in db
	timeExAccesstoken, err := time.ParseDuration(global.Config.Jwt.JWT_EXPIRATION)
	if global.Config.Jwt.JWT_EXPIRATION == "" || err != nil {
		timeExAccesstoken = time.Hour
	}
	go func() {
		errInsertToken := s.r.InsertAccessToken(ctx, database.InsertAccessTokenParams{
			ID:          uuid.New().String(),
			UserID:      userBase.UserID,
			CacheKey:    subToken,
			AccessToken: out.Token,
			ExpiresAt:   time.Now().Add(timeExAccesstoken),
		})
		if errInsertToken != nil {
			log.Println("Insert token failed: ", errInsertToken)
		}
	}()
	// save refresh token in db
	timeExRefreshtoken, err := time.ParseDuration(global.Config.Jwt.JWT_REFRESH_EXPIRED)
	if global.Config.Jwt.JWT_REFRESH_EXPIRED == "" || err != nil {
		timeExRefreshtoken = time.Hour * 24 * 7
	}
	go func() {
		errInsertRToken := s.r.InsertRefreshToken(ctx, database.InsertRefreshTokenParams{
			ID:           uuidRefreshToken,
			UserID:       userBase.UserID,
			RefreshToken: out.RefreshToken,
			ExpiresAt:    time.Now().Add(timeExRefreshtoken),
		})
		if errInsertRToken != nil {
			log.Println("Insert refresh token failed: ", errInsertRToken)
		}
	}()

	return response.ErrCodeSuccess, out, nil
}

// Register implements service.IUserLogin.
func (s *sUserLogin) Register(ctx context.Context, in *model.RegisterInput) (codeResult int, err error) {
	// logic
	// 1. hash email
	fmt.Printf("Verify key: %s\n", in.VerifyKey)
	fmt.Printf("Verify type: %d\n", in.VerifyType)

	hashKey := crypto.GetHash(strings.ToLower(in.VerifyKey))
	fmt.Printf("Hash key: %s\n", hashKey)

	// 2. check user exists in user database
	userFound, err := s.r.CheckUserBaseExists(ctx, in.VerifyKey)
	if err != nil {
		return response.ErrCodeUserHasExist, err
	}

	if userFound > 0 {
		return response.ErrCodeUserHasExist, errors.New("user has exist")
	}

	// 3. create otp
	userKey := utils.GetTwoFactorKeyVerifyRegister(hashKey) // fmt.Sprintf("u:%s:otp", key)
	otpFound, err := global.Rdb.Get(ctx, userKey).Result()

	fmt.Println("userKey::", userKey)
	fmt.Println("otpFound::", otpFound)
	// fmt.Println("Err:: ", err)
	// Check handle get otp in redis - TODO handle utils...
	switch {
	case errors.Is(err, redis.Nil):
		fmt.Println("key does not exist")
	case err != nil:
		fmt.Println("get failed:: ", err)
		return response.ErrInvalidOTP, err
	case otpFound != "":
		// TODO: check spam create otp and send notification
		return response.ErrCodeOTPNotExist, errors.New("OTP exists but not registered")
	}

	// 4. generate otp
	otpNew := random.GenerateSixDigitOtp()
	if in.VerifyPurpose == "TEST_USER" {
		otpNew = 123456
	}
	fmt.Printf("New OTP is ::: %d\n", otpNew)

	// 5. save otp in Redis with expiration time
	timeExpire := time.Duration(consts.TIME_OTP_REGISTER) * time.Hour
	err = global.Rdb.SetEx(ctx, userKey, strconv.Itoa(otpNew), timeExpire).Err()
	if err != nil {
		return response.ErrInvalidOTP, err
	}

	// 6. send otp
	switch in.VerifyType {
	case consts.EMAIL:
		// send email
		email_to := in.VerifyKey
		err = sendto.NewKafkaSendTo().SendKafkaEmailOTP(consts.EMAIL_HOST, email_to, consts.SEND_EMAIL_OTP, strconv.Itoa(otpNew))
		if err != nil {
			return response.ErrSendEmailOTP, err
		}
		global.Logger.Info(fmt.Sprintf("OTP is sent to email: %s sucess", email_to))

		// 7. save OTP to database
		result, err := s.r.InsertOTPVerify(
			ctx,
			database.InsertOTPVerifyParams{
				VerifyOtp:     strconv.Itoa(otpNew),
				VerifyKey:     in.VerifyKey,
				VerifyKeyHash: hashKey,
				VerifyType:    sql.NullInt32{Int32: 1, Valid: true},
			},
		)
		if err != nil {
			return response.ErrInvalidOTP, err
		}

		// 8. get last id
		lastIdVerifyUser, err := result.LastInsertId()
		if err != nil {
			return response.ErrSendEmailOTP, err
		}
		global.Logger.Info(fmt.Sprintf("Last id verify user: %d", lastIdVerifyUser))
	case consts.MOBILE:
		// send sms
		// TODO
		return response.ErrCodeSuccess, nil
	}

	return response.ErrCodeSuccess, nil
}

// VerifyOTP implements service.IUserLogin.
func (s *sUserLogin) VerifyOTP(ctx context.Context, in *model.VerifyInput) (out model.VerifyOTPOutput, err error) {
	// get hash key
	hashKey := crypto.GetHash(strings.ToLower(in.VerifyKey))

	// get otp
	otpFound, err := global.Rdb.Get(ctx, utils.GetTwoFactorKeyVerifyRegister(hashKey)).Result()
	switch {
	case errors.Is(err, redis.Nil):
		fmt.Println("key does not exist")
	case err != nil:
		fmt.Println("get failed:: ", err)
		return out, err
	}

	if in.VerifyCode != otpFound {
		// TODO - neu sai 3 lan trong 1 phut
		return out, errors.New("OTP not match")
	}

	infoOTP, err := s.r.GetInfoOTP(ctx, hashKey)
	if err != nil {
		return out, err
	}

	// upgrade status verify
	err = s.r.UpdateUserVerificationStatus(ctx, hashKey)
	if err != nil {
		return out, err
	}

	// output
	out.Token = infoOTP.VerifyKeyHash
	out.Message = "success"

	return out, err
}

// UpdatePasswordRegister implements service.IUserLogin.
func (s *sUserLogin) UpdatePasswordRegister(ctx context.Context, in *model.UpdatePasswordInput) (userId int, err error) {
	// token is already
	infoOTP, err := s.r.GetInfoOTP(ctx, in.Token)
	if err != nil {
		return response.ErrCodeUserOTPNotExist, err
	}
	// check otp verify
	if infoOTP.IsVerified.Int32 == 0 {
		return response.ErrCodeOTPDontVerify, errors.New("OTP not verify")
	}
	// check token exists in user_base
	// update userbase password
	salt, err := crypto.GenerateSalt(16)
	if err != nil {
		return response.ErrCodeGeneratorSalt, err
	}
	passworkHash := crypto.HashPasswordWithSalt(in.Password, salt)

	userBase := database.AddUserBaseWithUUIDParams{
		UserID:       uuid.New().String(),
		UserAccount:  infoOTP.VerifyKey,
		UserPassword: passworkHash,
		UserSalt:     salt,
	}
	// add userBase to user_base table
	_, err = s.r.AddUserBaseWithUUID(ctx, userBase)
	if err != nil {
		return response.ErrCodeAddUserBase, err
	}

	// add user_id to user_info table
	_, err = s.r.AddUserHaveUserId(ctx, database.AddUserHaveUserIdParams{
		UserID:       userBase.UserID,
		UserAccount:  infoOTP.VerifyKey,
		UserNickname: sql.NullString{String: infoOTP.VerifyKey, Valid: true},
		UserAvatar:   sql.NullString{String: "", Valid: true},
		UserState:    database.UserInfoUserStateActivated,
		UserMobile:   sql.NullString{String: "", Valid: true},
		UserGender:   database.NullUserInfoUserGender{UserInfoUserGender: database.UserInfoUserGenderMale, Valid: false},
		UserBirthday: sql.NullTime{Time: time.Time{}, Valid: false},
		UserEmail:    sql.NullString{String: infoOTP.VerifyKey, Valid: true},
	})
	if err != nil {
		return response.ErrCodeAddUserInfo, err
	}

	return response.ErrCodeSuccess, nil
}
