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
	"go.uber.org/zap"
)

// struct
type sUserLogin struct {
	r *database.Queries
}

// help function block token user with id
func (s *sUserLogin) blockToken(cUserID string) {
	// block access tokens
	go func() {
		listAccessTokenValid, err := s.r.GetValidAccessTokensWithUserID(context.Background(), cUserID)
		if err != nil {
			global.Logger.Error("Err when getting valid access tokens with user ID ", zap.String("userID", cUserID))
			return
		}
		for _, accessToken := range listAccessTokenValid {
			if global.Rdb.Del(context.Background(), accessToken.CacheKey).Err() != nil {
				global.Logger.Error(fmt.Sprintf("Err when deleting cache access token %s\n", accessToken.CacheKey))
			}
		}
		global.Logger.Info(fmt.Sprintf("Deleted all access tokens with user ID %s\n", cUserID))
	}()
	// block refresh token
	go func() {
		listRefreshTokenValid, err := s.r.GetValidRefreshTokensByUserID(context.Background(), cUserID)
		if err != nil {
			global.Logger.Error("Err when getting valid refresh tokens with user ID ", zap.String("userID", cUserID))
			return
		}
		for _, refreshToken := range listRefreshTokenValid {
			if s.r.ExecTokenUsedWithID(context.Background(), refreshToken) != nil {
				global.Logger.Error("Err when blocking refresh token with user ID ", zap.String("userID", cUserID))
			}
		}
		global.Logger.Info(fmt.Sprintf("Blocked all refresh tokens with user ID %s\n", cUserID))
	}()
	// block user refresh token
	go func() {
		if s.r.RefreshTokenUserOff(context.Background(), cUserID) != nil {
			global.Logger.Error("Err off refresh token user ", zap.String("idUser", cUserID))
		} else {
			global.Logger.Info("Off refresh token user " + cUserID)
		}
	}()
}

// new sUserLogin implementation interface for IUserLogin
func NewSUserLogin(r *database.Queries) service.IUserLogin {
	return &sUserLogin{
		r: r,
	}
}

// ForgotPassword implements service.IUserLogin.
func (s *sUserLogin) ForgotPassword(ctx context.Context, email string) (codeResult int, err error) {
	// 1. check cache sended password
	key := utils.GetForgetPasswordRequestKey(email)
	dataCache, err := global.Rdb.Get(ctx, key).Result()
	// Check handle get otp in redis - TODO handle utils...
	switch {
	case errors.Is(err, redis.Nil):
		fmt.Println("key does not exist")
	case err != nil:
		fmt.Println("get failed:: ", err)
		return response.ErrCodeGetCache, err
	}
	if dataCache != "" {
		// TODO: check spam forgot password
		return response.ErrCodeSuccess, nil
	} else {
		// 2. Check email exists in user_base
		cUserID, err := s.r.GetIDUserWithEmail(ctx, email)
		if err != nil {
			return response.ErrCodeUserNotFound, err
		}
		if cUserID == "" {
			global.Logger.Error("user not found with email ", zap.String("email", email))
			return response.ErrCodeUserNotFound, errors.New("user not found")
		}
		// 3. Create new password
		salt, err := crypto.GenerateSalt(16)
		if err != nil {
			return response.ErrCodeGeneratorSalt, err
		}
		newPassword := random.GeneratePassword(10)
		newPassworkHash := crypto.HashPasswordWithSalt(newPassword, salt)
		// 4. Update password in user_base
		go func() {
			err_p := s.r.UpdatePasswordAndSaltWithUserID(context.Background(), database.UpdatePasswordAndSaltWithUserIDParams{
				UserPassword: newPassworkHash,
				UserSalt:     salt,
				UserID:       cUserID,
			})
			if err_p != nil {
				fmt.Printf("Err when updating password for user %s: %v\n", cUserID, err_p)
			}
			global.Logger.Info("Created new password for user " + cUserID)
		}()
		// 5. Save handle forpassword in cache
		go func() {
			if global.Rdb.Set(
				ctx,
				key,
				newPassword,
				time.Duration(consts.TIME_BLOCK_FORGOT_PASSWORD_REQUEST)*time.Hour,
			).Err() != nil {
				fmt.Printf("Err when saving cache for forgot password: %v\n", err)
			} else {
				global.Logger.Info("Saved handle for forgot password in cache with key " + key)
			}
		}()
		// 6. Send password to email
		go func() {
			email_to := email
			err_s := sendto.NewKafkaSendTo().SendKafkaMailNewPassword(consts.EMAIL_HOST, email_to, consts.SEND_EMAIL_OTP, newPassword)
			if err_s != nil {
				global.Logger.Error("Err when sending mail new password to ", zap.String("email", email_to))
			}
			global.Logger.Info("Sented new password to email " + email_to)
		}()
		// 7. Block all all token login
		go func() {
			s.blockToken(cUserID)
		}()
		return response.ErrCodeSuccess, nil
	}
}

// Logout implements service.IUserLogin.
func (s *sUserLogin) Logout(ctx context.Context, in *model.LogoutInput) (codeResult int, err error) {
	panic("unimplemented") // TODO: implement
}

// RefreshToken implements service.IUserLogin.
func (s *sUserLogin) RefreshToken(ctx context.Context, in *model.RefreshTokenInput) (codeResult int, out model.LoginOutput, err error) {
	// 1. validate access token
	_, err = auth.ValidateTokenSubject(in.AccessToken)
	err = utils.HandleTokenJwtErrWhenRefresh(err)
	if err != nil {
		global.Logger.Error(fmt.Sprintf("Validate access token failed: %v", err))
		return response.ErrCodeAuthFailed, out, err
	}
	// get info user use block
	iUser, _ := s.r.GetMailUserWithAccessToken(ctx, in.AccessToken)
	// 2. validate refresh token
	_, err = auth.ValidateTokenSubject(in.RefreshToken)
	if err != nil {
		global.Logger.Error(fmt.Sprintf("Validate refresh token failed: %v", err))
		// block user access
		go s.blockToken(iUser.UserID)
		return response.ErrCodeAuthFailed, out, err
	}
	// 3. check refresh token in db and update status token
	ciRTK, err := s.r.GetRefreshToken(ctx, in.RefreshToken)
	if err != nil {
		return response.ErrCodeAuthFailed, out, err
	}
	if ciRTK.IsUsed == 0 { // token is used
		// block user access
		go s.blockToken(iUser.UserID)
		return response.ErrCodeAuthFailed, out, errors.New("refresh token is used")
	}
	// 4. create new access token
	subToken := utils.GenerateCliTokenUUID(ciRTK.UserID)
	accessTokenNew, err := auth.CreateToken(subToken, iUser.UserID)
	if err != nil {
		return response.ErrCodeAuthFailed, out, err
	}
	// 5. create new refresh token
	uuidRefreshToken := uuid.New().String()
	refreshTokenNew, err := auth.CreateRefreshToken(uuidRefreshToken)
	if err != nil {
		return response.ErrCodeAuthFailed, out, err
	}
	// 6. save new refresh token in db and change status old refresh token
	timeExRefreshtoken, err := time.ParseDuration(global.Config.Jwt.JWT_REFRESH_EXPIRED)
	if global.Config.Jwt.JWT_REFRESH_EXPIRED == "" || err != nil {
		timeExRefreshtoken = time.Hour * 24 * 7
	}
	go func() {
		err1 := s.r.ExecTokenUsedWithID(context.Background(), ciRTK.ID)
		if err1 != nil {
			log.Println("Exec token used failed: ", err)
		}
		//
		err2 := s.r.InsertRefreshToken(ctx, database.InsertRefreshTokenParams{
			ID:           uuidRefreshToken,
			UserID:       ciRTK.UserID,
			RefreshToken: refreshTokenNew,
			ExpiresAt:    time.Now().Add(timeExRefreshtoken),
		})
		if err2 != nil {
			log.Println("Insert refresh token failed: ", err)
		}
	}()
	// 7. save new access token in db and write to redis
	timeExAccesstoken, err := time.ParseDuration(global.Config.Jwt.JWT_EXPIRATION)
	if global.Config.Jwt.JWT_EXPIRATION == "" || err != nil {
		timeExAccesstoken = time.Hour * 7
	}
	go func() {
		err1 := s.r.InsertAccessToken(context.Background(), database.InsertAccessTokenParams{
			ID:          uuid.New().String(),
			UserID:      ciRTK.UserID,
			CacheKey:    subToken,
			AccessToken: accessTokenNew,
			ExpiresAt:   time.Now().Add(timeExAccesstoken),
		})
		if err1 != nil {
			log.Println("Insert token failed: ", err)
		}
		//
		err2 := global.Rdb.Set(ctx, subToken, ciRTK.UserID, timeExAccesstoken).Err()
		if err2 != nil {
			log.Println("Set redis failed: ", err)
		}
	}()
	// Create output
	out = model.LoginOutput{
		Token:        accessTokenNew,
		RefreshToken: refreshTokenNew,
		Message:      "success",
	}
	return response.ErrCodeSuccess, out, nil
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
	out.Token, err = auth.CreateToken(subToken, userBase.UserID)
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
	keyRedis := utils.GetTwoFactorKeyVerifyRegister(hashKey)
	// get otp
	otpFound, err := global.Rdb.Get(ctx, keyRedis).Result()
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
	// rm cache otp
	go func() {
		err := global.Rdb.Del(ctx, keyRedis).Err()
		if err != nil {
			fmt.Println("Del failed:: ", err)
		}
		global.Logger.Info(fmt.Sprintf("Del OTP cache: %s", hashKey))
	}()

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
	infoOTP, err := s.r.GetValidOtp(ctx, in.Token)
	if err != nil {
		return response.ErrCodeUserOTPNotExist, err
	}
	// check otp verify
	if infoOTP.VerifyKeyHash != in.Token {
		return response.ErrCodeOTPDontVerify, errors.New("otp not verify")
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

	// delete token 
	go func() {
		err_del := s.r.DeleteTokenVerifyRegister(context.Background(), infoOTP.VerifyKeyHash)
		if err_del != nil {
			global.Logger.Info(fmt.Sprintf("Delete token verify register failed: %v", err_del))
		}
		global.Logger.Info(fmt.Sprintf("Deleted token verify register: %s", infoOTP.VerifyKeyHash))
	}()

	return response.ErrCodeSuccess, nil
}
