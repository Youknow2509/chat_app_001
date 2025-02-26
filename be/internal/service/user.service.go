package service

import (
	"context"

	"example.com/be/internal/model"
)

// create interface
type (
	IUserLogin interface {
		Login(ctx context.Context, in *model.LoginInput) (codeResult int, out model.LoginOutput, err error)
		Register(ctx context.Context, in *model.RegisterInput) (codeResult int, err error)
		VerifyOTP(ctx context.Context, in *model.VerifyInput) (out model.VerifyOTPOutput, err error)
		UpdatePasswordRegister(ctx context.Context, in *model.UpdatePasswordInput) (userId int, err error)
	}

	IUserInfo interface { // handle with token
		GetUserInfo(ctx context.Context, userID string) (out model.UserInfoOutput, err error)
		FindUser(ctx context.Context, email string) (out model.UserInfoOutput, err error)
		UpdateUserInfo(ctx context.Context, in *model.UpdateUserInfoInput) (codeResult int, err error)
		// AddFriendRequest(ctx context.Context, int *model.AddFriendRequestInput) (codeResult int, err error)
		// ResponseFriendRequest(ctx context.Context, friendId string) (codeResult int, err error)
		CreateFriendRequest(ctx context.Context, in *model.CreateFriendRequestInput) (codeResult int, err error)
		EndFriendRequest(ctx context.Context, in *model.EndFriendRequestInput) (codeResult int, err error)
		DeleteFriend(ctx context.Context, in *model.DeleteFriendInput) (codeResult int, err error)
		AcceptFriendRequest(ctx context.Context, in *model.AcceptFriendRequestInput) (codeResult int, err error)
		RejectFriendRequest(ctx context.Context, in *model.RejectFriendRequestInput) (codeResult int, err error)
		GetListFriendRequest(ctx context.Context, in *model.GetFriendRequestInput) (out []model.GetListFriendRequestOutput, err error)
	}

	IUserAdmin interface {
	}
)

// variables for service interface
var (
	localUserLogin IUserLogin
	localUserInfo  IUserInfo
	localUserAdmin IUserAdmin
)

/**
 * Handle interface IUserLogin
 */
// Get interface IUser
func UserLogin() IUserLogin {
	if localUserLogin == nil {
		panic("implement localuserlogin not found for interface IUserLogin")
	}
	return localUserLogin
}

// Init interface IUserLogin
func InitUserLogin(userLogin IUserLogin) {
	localUserLogin = userLogin
}

/**
 * Handle interface IUserInfo
 */
// Get interface IUserInfo
func UserInfo() IUserInfo {
	if localUserInfo == nil {
		panic("implement localuserInfo not found for interface IUserInfo")
	}
	return localUserInfo
}

// Init interface IUserInfo
func InitUserInfo(userInfo IUserInfo) {
	localUserInfo = userInfo
}

/**
 * Handle interface IUserAdmin
 */
// Get interface IUserAdmin
func UserAdmin() IUserAdmin {
	if localUserAdmin == nil {
		panic("implement localuserAdmin not found for interface IUserAdmin")
	}
	return localUserAdmin
}

// Init interface IUserAdmin
func InitUserAdmin(userAdmin IUserAdmin) {
	localUserAdmin = userAdmin
}
