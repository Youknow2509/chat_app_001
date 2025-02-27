package impl

import (
	"context"
	"database/sql"
	"encoding/json"
	"errors"
	"fmt"
	"time"

	"example.com/be/global"
	"example.com/be/internal/consts"
	"example.com/be/internal/database"
	"example.com/be/internal/model"
	"example.com/be/internal/service"
	"example.com/be/internal/utils"
	"example.com/be/internal/utils/crypto"
	"example.com/be/response"
	"github.com/google/uuid"
	"github.com/redis/go-redis/v9"
	"go.uber.org/zap"
)

// struct
type sUserInfo struct {
	r *database.Queries
}

// UpdatePasswordForUserRequest implements service.IUserInfo.
func (s *sUserInfo) UpdatePasswordForUserRequest(ctx context.Context, in *model.UserChangePasswordInput) (codeResult int, err error) {
	// 1. check user exist
	cUserReq, err := s.r.CheckUserBaseExistsWithID(ctx, in.UserID)
	if err != nil {
		global.Logger.Error("Err get user with id", zap.Error(err))
		return response.ErrCodeUserNotFound, err
	}
	if cUserReq < 1 {
		global.Logger.Error("User is not exist")
		return response.ErrCodeUserNotFound, fmt.Errorf("user is not exist")
	}
	// 2. get password old
	iPasswordSalt, err := s.r.GetPasswordSaltWithUserID(ctx, in.UserID)
	if err != nil {
		global.Logger.Error("Err get password salt", zap.Error(err))
		return response.ErrCodeGetPasswordSalt, err
	}
	passwordSendHash := crypto.HashPasswordWithSalt(in.OldPassword, iPasswordSalt.UserSalt)
	if passwordSendHash != iPasswordSalt.UserPassword {
		global.Logger.Error("Old password is not correct")
		return response.ErrCodePasswordIncorrect, errors.New("old password is not correct")
	}
	// 3. update password
	go func() {
		passworkNewHash := crypto.HashPasswordWithSalt(in.NewPassword, iPasswordSalt.UserSalt)
        err = s.r.UpdatePasswordWithUserID(context.Background(), database.UpdatePasswordWithUserIDParams{
            UserID: in.UserID,
            UserPassword: passworkNewHash,
        })
        if err != nil {
            fmt.Printf("Err when updating password for user %s \n", in.UserID)
        }
	}()
	return response.ErrCodeSuccess, nil
}

// GetListFriendRequest implements service.IUserInfo.
func (s *sUserInfo) GetListFriendRequest(ctx context.Context, in *model.GetFriendRequestInput) (out []model.GetListFriendRequestOutput, err error) {
	// 1. check user exist
	cUserReq, err := s.r.CheckUserBaseExistsWithID(ctx, in.UserID)
	if err != nil {
		global.Logger.Error("Err get user with id", zap.Error(err))
		return nil, err
	}
	if cUserReq < 1 {
		global.Logger.Error("User is not exist")
		return nil, fmt.Errorf("user is not exist")
	}
	// 2. get friend request
	listFriendUser, err := s.r.GetFriendRequestUserReceive(ctx, database.GetFriendRequestUserReceiveParams{
		ToUser: sql.NullString{String: in.UserID, Valid: true},
		Limit:  int32(in.Limit),
		Offset: int32(utils.GetOffsetWithLimit(in.Page, in.Limit)),
	})
	if err != nil {
		global.Logger.Error("Err get friend request", zap.Error(err))
		return nil, err
	}
	// 3. write output
	out = make([]model.GetListFriendRequestOutput, 0, len(listFriendUser))
	for _, item := range listFriendUser {
		var createdAt string
		if item.CreatedAt.Valid {
			createdAt = item.CreatedAt.Time.Format(time.RFC3339)
		} else {
			createdAt = ""
		}
		out = append(out, model.GetListFriendRequestOutput{
			RequestID:     item.ID,
			FromUser:      item.FromUser.String,
			ToUser:        "you",
			StatusRequest: item.Status.String,
			CreatedAt:     createdAt,
		})
	}
	// 4. save to cache
	go func() {
		cacheData, err := json.Marshal(out)
		if err != nil {
			global.Logger.Error("Err marshal data", zap.Error(err))
			return
		}
		key := fmt.Sprintf("list_friend_request::to::%s::l%d::p%d", in.UserID, in.Limit, in.Page)
		timeEx := time.Duration(consts.TIME_SAVE_CACHE_OFTEN_USE) * time.Minute
		err = global.Rdb.Set(context.Background(), key, cacheData, timeEx).Err()
		if err != nil {
			fmt.Printf("err when saving cache friend request to %s\n", in.UserID)
		}
	}()
	return out, nil
}

// AcceptFriendRequest implements service.IUserInfo.
func (s *sUserInfo) AcceptFriendRequest(ctx context.Context, in *model.AcceptFriendRequestInput) (codeResult int, err error) {
	// 1. get request
	cInfoRequest, err := s.r.GetFriendRequestInfo(ctx, in.RequestID)
	if err != nil {
		global.Logger.Error("Err get friend request info", zap.Error(err))
		return response.ErrCodeCheckFriendRequest, err
	}
	if cInfoRequest.ID == "" {
		global.Logger.Error("Friend request not found")
		return response.ErrCodeFrinedRequestNotFound, fmt.Errorf("friend request not found")
	}
	// 2. check user accept
	if cInfoRequest.ToUser.String != in.UserAcceptID {
		global.Logger.Error("User accept not match")
		return response.ErrCodeUserNotFound, fmt.Errorf("user accept not match")
	}
	// 3. update status request accept
	go func() {
		err := s.r.AcceptFriendRequest(context.Background(), in.RequestID)
		if err != nil {
			fmt.Println("Err when accepting friend request")
		}
	}()
	// 4. add friend
	go func() {
		err := s.r.AddFriend(context.Background(), database.AddFriendParams{
			UserID:   sql.NullString{String: cInfoRequest.FromUser.String, Valid: true},
			FriendID: sql.NullString{String: cInfoRequest.ToUser.String, Valid: true},
		})
		if err != nil {
			fmt.Println("Err when adding friend")
		}
	}()
	// 5. delete cache list friend request
	go func() {
		key1 := fmt.Sprintf("list_friend_request::to::%s", cInfoRequest.FromUser.String)
		key2 := fmt.Sprintf("list_friend_request::to::%s", cInfoRequest.ToUser.String)

		err1 := utils.DeleteCacheWithKeyPrefix(key1)
		err2 := utils.DeleteCacheWithKeyPrefix(key2)

		if err1 != nil {
			fmt.Printf("Err when deleting cache friend request from user %s\n", cInfoRequest.FromUser.String)
		}
		if err2 != nil {
			fmt.Printf("Err when deleting cache friend request from user %s\n", cInfoRequest.ToUser.String)
		}
	}()
	// TODO: send notification when accept friend request to user send
	return response.ErrCodeSuccess, nil
}

// CreateFriendRequest implements service.IUserInfo.
func (s *sUserInfo) CreateFriendRequest(ctx context.Context, in *model.CreateFriendRequestInput) (codeResult int, err error) {
	// 1. check user send with id exists
	cUserSend, err := s.r.CheckUserBaseExistsWithID(ctx, in.UserID)
	if err != nil {
		global.Logger.Error("Err get user with id", zap.Error(err))
		return response.ErrCodeUserNotFound, err
	}
	if cUserSend < 1 {
		global.Logger.Error("User is not exist")
		return response.ErrCodeUserNotFound, fmt.Errorf("user is not exist")
	}
	// 2. check friend exist with email address
	iUserFriend, err := s.r.GetOneUserInfo(ctx, in.EmailFriend)
	if err != nil {
		global.Logger.Error("Err get user info", zap.Error(err))
		return response.ErrCodeUserNotFound, err
	}
	if iUserFriend.UserID == "" {
		global.Logger.Error("Friend not found")
		return response.ErrCodeUserNotFound, fmt.Errorf("friend not found")
	}
	if iUserFriend.UserID == in.UserID {
		global.Logger.Error("Can not add friend yourself")
		return response.ErrCodeUserBlockAddFriendRequest, fmt.Errorf("can not add friend yourself")
	}
	// 3. check request exist
	cReq, err := s.r.CheckFriendRequestExists(ctx, database.CheckFriendRequestExistsParams{
		FromUser: sql.NullString{String: in.UserID, Valid: true},
		ToUser:   sql.NullString{String: iUserFriend.UserID, Valid: true},
		//
		FromUser_2: sql.NullString{String: iUserFriend.UserID, Valid: true},
		ToUser_2:   sql.NullString{String: in.UserID, Valid: true},
	})
	if err != nil {
		global.Logger.Error("Err check friend request exists", zap.Error(err))
		return response.ErrCodeCheckFriendRequest, err
	}
	if cReq > 0 {
		global.Logger.Error("Friend request exists")
		return response.ErrCodeUserBlockAddFriendRequest, fmt.Errorf("friend request exists")
	}
	// 4. check block cache
	key := utils.GetBlockFriendRequestKey(in.UserID, iUserFriend.UserID)
	err = global.Rdb.Get(ctx, key).Err()
	switch {
	case errors.Is(err, redis.Nil):
		fmt.Println("key does not exist")
	case err != nil:
		fmt.Println("get failed:: ", err)
		return response.ErrCodeUserBlockAddFriendRequest, err
	}
	// 5. create friend request
	go func() {
		err = s.r.InsertFriendRequest(context.Background(), database.InsertFriendRequestParams{
			ID:       uuid.New().String(),
			FromUser: sql.NullString{String: in.UserID, Valid: true},
			ToUser:   sql.NullString{String: iUserFriend.UserID, Valid: true},
		})
		if err != nil {
			fmt.Println("Err when creating friend request")
		}
	}()
	// 6. delete cache list friend request
	go func() {
		key1 := fmt.Sprintf("list_friend_request::to::%s", in.UserID)
		key2 := fmt.Sprintf("list_friend_request::to::%s", iUserFriend.UserID)

		err1 := utils.DeleteCacheWithKeyPrefix(key1)
		err2 := utils.DeleteCacheWithKeyPrefix(key2)

		if err1 != nil {
			fmt.Printf("Err when deleting cache friend request from user %s\n", in.UserID)
		}
		if err2 != nil {
			fmt.Printf("Err when deleting cache friend request from user %s\n", iUserFriend.UserID)
		}
	}()
	return response.ErrCodeSuccess, nil
}

// DeleteFriend implements service.IUserInfo.
func (s *sUserInfo) DeleteFriend(ctx context.Context, in *model.DeleteFriendInput) (codeResult int, err error) {
	// 1. get id user from email
	idFriend, err := s.r.GetIDUserWithEmail(ctx, in.FriendEmail)
	if err != nil {
		global.Logger.Error("Err get user with id", zap.Error(err))
		return response.ErrCodeUserNotFound, err
	}
	if idFriend == "" {
		global.Logger.Error("Friend not found")
		return response.ErrCodeUserNotFound, fmt.Errorf("friend not found")
	}
	// 2. delete friend
	go func() {
		err = s.r.DeleteFriend(context.Background(), database.DeleteFriendParams{
			UserID:   sql.NullString{String: in.UserID, Valid: true},
			FriendID: sql.NullString{String: idFriend, Valid: true},
			//
			UserID_2:   sql.NullString{String: idFriend, Valid: true},
			FriendID_2: sql.NullString{String: in.UserID, Valid: true},
		})
		if err != nil {
			fmt.Println("Err when deleting friend")
		}
	}()

	return response.ErrCodeSuccess, nil
}

// EndFriendRequest implements service.IUserInfo.
func (s *sUserInfo) EndFriendRequest(ctx context.Context, in *model.EndFriendRequestInput) (codeResult int, err error) {
	// 1. check request exist
	cInfoRequest, err := s.r.GetFriendRequestInfo(ctx, in.RequestID)
	if err != nil {
		global.Logger.Error("Err get friend request info", zap.Error(err))
		return response.ErrCodeCheckFriendRequest, err
	}
	if cInfoRequest.ID == "" {
		global.Logger.Error("Friend request not found")
		return response.ErrCodeFrinedRequestNotFound, fmt.Errorf("friend request not found")
	}
	// 2. delete request
	go func() {
		err := s.r.DeleteFriendRequest(context.Background(), in.RequestID)
		if err != nil {
			fmt.Println("Err when deleting friend request")
		}
	}()
	// 3. delete cache list friend reque
	go func() {
		key1 := fmt.Sprintf("list_friend_request::to::%s", cInfoRequest.FromUser.String)
		key2 := fmt.Sprintf("list_friend_request::to::%s", cInfoRequest.ToUser.String)

		err1 := utils.DeleteCacheWithKeyPrefix(key1)
		err2 := utils.DeleteCacheWithKeyPrefix(key2)

		if err1 != nil {
			fmt.Printf("Err when deleting cache friend request from user %s\n", cInfoRequest.FromUser.String)
		}
		if err2 != nil {
			fmt.Printf("Err when deleting cache friend request from user %s\n", cInfoRequest.ToUser.String)
		}
	}()
	return response.ErrCodeSuccess, nil
}

// FindUser implements service.IUserInfo.
func (s *sUserInfo) FindUser(ctx context.Context, email string) (out model.UserInfoOutput, err error) {
	// 1. check uset exist
	iUser, err := s.r.GetUserWithAccount(ctx, email)
	if err != nil {
		global.Logger.Error("Err get user info", zap.Error(err))
		return out, err
	}
	if iUser.UserID == "" {
		global.Logger.Error("User not found")
		return out, fmt.Errorf("user not found")
	}
	// 2. write out request
	out = model.UserInfoOutput{
		UserID:       iUser.UserID,
		UserAccount:  iUser.UserAccount,
		UserNickname: iUser.UserNickname.String,
		UserAvatar:   iUser.UserAccount,
		UserState:    string(iUser.UserState),
		UserMobile:   iUser.UserMobile.String,
		UserGender:   string(iUser.UserGender.UserInfoUserGender),
		UserBirthday: iUser.UserBirthday.Time,
		UserEmail:    iUser.UserEmail.String,
		CreatedAt:    iUser.CreatedAt.Time,
		UpdatedAt:    iUser.UpdatedAt.Time,
	}
	// 3. save to cache
	return out, nil
}

// GetUserInfo implements service.IUserInfo.
func (s *sUserInfo) GetUserInfo(ctx context.Context, userID string) (out model.UserInfoOutput, err error) {
	// 1. check cache data
	key := fmt.Sprintf("user_info::%s", userID)
	cacheData, err := global.Rdb.Get(ctx, key).Result()
	// Check handle get data in redis - TODO handle utils...
	switch {
	case errors.Is(err, redis.Nil):
		fmt.Println("key does not exist")
	case err != nil:
		fmt.Println("get failed:: ", err)
		return out, err
	}
	if cacheData != "" {
		err = json.Unmarshal([]byte(cacheData), &out)
		if err != nil {
			fmt.Println("unmarshal failed:: ", err)
			return out, err
		}
		return out, nil
	} else {
		// 2. check uset exist
		iUser, err := s.r.GetUserWithID(ctx, userID)
		if err != nil {
			global.Logger.Error("Err get user info", zap.Error(err))
			return out, err
		}
		if iUser.UserID == "" {
			global.Logger.Error("User not found")
			return out, fmt.Errorf("user not found")
		}
		// 3. write out request
		out = model.UserInfoOutput{
			UserID:       iUser.UserID,
			UserAccount:  iUser.UserAccount,
			UserNickname: iUser.UserNickname.String,
			UserAvatar:   iUser.UserAccount,
			UserState:    string(iUser.UserState),
			UserMobile:   iUser.UserMobile.String,
			UserGender:   string(iUser.UserGender.UserInfoUserGender),
			UserBirthday: iUser.UserBirthday.Time,
			UserEmail:    iUser.UserEmail.String,
			CreatedAt:    iUser.CreatedAt.Time,
			UpdatedAt:    iUser.UpdatedAt.Time,
		}
		// 4. save to cache
		go func() {
			timeEx := time.Duration(consts.TIME_SAVE_CACHE_OFTEN_USE) * time.Hour
			cacheInput, err := json.Marshal(out)
			if err != nil {
				fmt.Printf("Err when marshal cache user info %s\n", iUser.UserID)
				return
			}
			err = global.Rdb.Set(context.Background(), key, cacheInput, timeEx).Err()
			if err != nil {
				fmt.Printf("Err when save cache user info %s\n", iUser.UserID)
			}
		}()
	}
	return out, nil
}

// RejectFriendRequest implements service.IUserInfo.
func (s *sUserInfo) RejectFriendRequest(ctx context.Context, in *model.RejectFriendRequestInput) (codeResult int, err error) {
	// 1. get request info
	cInfoRequest, err := s.r.GetFriendRequestInfo(ctx, in.RequestID)
	if err != nil {
		global.Logger.Error("Err get friend request info", zap.Error(err))
		return response.ErrCodeCheckFriendRequest, err
	}
	if cInfoRequest.ID == "" {
		global.Logger.Error("Friend request not found")
		return response.ErrCodeFrinedRequestNotFound, fmt.Errorf("friend request not found")
	}
	// 2. check user rejects
	if cInfoRequest.ToUser.String != in.UserAcceptID {
		global.Logger.Error("User reject not match")
		return response.ErrCodeUserNotFound, fmt.Errorf("user reject not match")
	}
	// 3. reject request
	go func() {
		err = s.r.DeclineFriendRequest(context.Background(), in.RequestID)
		if err != nil {
			fmt.Println("Err when rejecting friend request")
		}
	}()
	// 4. delete cache list friend request
	go func() {
		key1 := fmt.Sprintf("list_friend_request::to::%s", cInfoRequest.FromUser.String)
		key2 := fmt.Sprintf("list_friend_request::to::%s", cInfoRequest.ToUser.String)

		err1 := utils.DeleteCacheWithKeyPrefix(key1)
		err2 := utils.DeleteCacheWithKeyPrefix(key2)

		if err1 != nil {
			fmt.Printf("Err when deleting cache friend request from user %s\n", cInfoRequest.FromUser.String)
		}
		if err2 != nil {
			fmt.Printf("Err when deleting cache friend request from user %s\n", cInfoRequest.ToUser.String)
		}
	}()
	// 5. write cache block spam request in cache
	go func() {
		// key := fmt.Sprintf("friend_requested::from::%s::to::%s", cInfoRequest.FromUser.String, cInfoRequest.ToUser.String)
		key := utils.GetBlockFriendRequestKey(cInfoRequest.FromUser.String, cInfoRequest.ToUser.String)
		timeEx := time.Duration(consts.TIME_BLOCK_CREATE_FRIEND_REQUEST) * time.Hour
		err := global.Rdb.Set(context.Background(), key, "true", timeEx).Err()
		if err != nil {
			fmt.Printf("Err when blocking spam friend request from %s to %s\n", cInfoRequest.FromUser.String, cInfoRequest.ToUser.String)
		}
	}()
	return response.ErrCodeSuccess, nil
}

// UpdateUserInfo implements service.IUserInfo.
func (s *sUserInfo) UpdateUserInfo(ctx context.Context, in *model.UpdateUserInfoInput) (codeResult int, err error) {
	// 1. check user info exist
	cUserBase, err := s.r.CheckUserBaseExistsWithID(ctx, in.UserID)
	if err != nil {
		global.Logger.Error("Err check user base exists", zap.Error(err))
		return response.ErrCodeUserBaseNotFound, err
	}
	if cUserBase < 1 {
		global.Logger.Error("User not found", zap.Error(err))
		return response.ErrCodeUserNotFound, fmt.Errorf("user not found")
	}
	// 2. update user info
	go func() {
		err := s.r.EditUserByUserIdForUser(context.Background(), database.EditUserByUserIdForUserParams{
			UserID:       in.UserID,
			UserNickname: sql.NullString{String: in.UserNickName, Valid: true},
			UserAvatar:   sql.NullString{String: in.UserAvatar, Valid: true},
			UserMobile:   sql.NullString{String: "", Valid: true},
		})
		if err != nil {
			fmt.Println("Err when updating user info")
		}
	}()
	// 3. delete cache info user request
	go func() {
		key := fmt.Sprintf("user_info::%s", in.UserID)
		err := global.Rdb.Del(context.Background(), key)
		if err != nil {
			fmt.Printf("Err when deleting cache user info %s\n", in.UserID)
		}
	}()
	return response.ErrCodeSuccess, nil
}

// new struct and implementation interface for IUserInfo
func NewSUserInfo(r *database.Queries) service.IUserInfo {
	return &sUserInfo{
		r: r,
	}
}
