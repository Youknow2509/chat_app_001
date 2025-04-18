package model

import "time"

type GetListFriendRequestSendOutput struct {
	RequestID     string `json:"request_id"`
	ToUser        string `json:"to_user"`
	UserNickName  string `json:"user_nickname"`
	UserAvatar    string `json:"user_avatar"`
	StatusRequest string `json:"status_request"`
	CreatedAt     string `json:"created_at"`
}

// UpdateAvatarUserInput
type UpdateAvatarUserInput struct {
	UserID    string `json:"user_id"`
	AvatarURL string `json:"avatar_url"`
}

type UserInfoBase struct {
	UserID       string `json:"user_id"`
	UserAccount  string `json:"user_account"`
	UserNickname string `json:"user_nickname"`
	UserAvatar   string `json:"user_avatar"`
}

// intput check friend user
type CheckFriendUserInput struct {
	User1 string `json:"user_1"`
	User2 string `json:"user_2"`
}

// output check friend user
type CheckFriendUserOutput struct {
	FriendID string    `json:"friend_id"`
	User1    string    `json:"user_1"`
	User2    string    `json:"user_2"`
	Status   string    `json:"status"`
	CreateAt time.Time `json:"created_at"`
	UpdateAt time.Time `json:"updated_at"`
}

// VerifyForgotPassword input
type VerifyForgotPasswordInput struct {
	Email string `json:"email"`
	Token string `json:"token"`
}

// list user friend input
type ListUserFriendInput struct {
	UserID string `json:"user_id"`
	Limit  int    `json:"limit"`
	Page   int    `json:"page"`
}

// UserInfoOutput
type UserInfoOutput struct {
	UserID       string    `json:"user_id"`
	UserAccount  string    `json:"user_account"`
	UserNickname string    `json:"user_nickname"`
	UserAvatar   string    `json:"user_avatar"`
	UserState    string    `json:"user_state"`
	UserMobile   string    `json:"user_mobile"`
	UserGender   string    `json:"user_gender"`
	UserBirthday time.Time `json:"user_birthday"`
	UserEmail    string    `json:"user_email"`
	CreatedAt    time.Time `json:"created_at"`
	UpdatedAt    time.Time `json:"updated_at"`
}

// user find output
type UserFindOutput struct {
	UserID       string `json:"user_id"`
	UserNickname string `json:"user_nickname"`
	UserAvatar   string `json:"user_avatar"`
	UserEmail    string `json:"user_email"`
}

// user find input
type UserFindInput struct {
	UserEmail string `json:"user_email"`
	Limit     int    `json:"limit"`
	Page      int    `json:"page"`
}

// UpdateUserInfoInput
type UpdateUserInfoInput struct {
	UserID       string `json:"user_id"`
	UserNickName string `json:"user_nickname"`
	UserBirthday string `json:"user_birthday"`
	UserGender   string `json:"user_gender"`
}

// handle add user friend request input
type AddFriendRequestInput struct {
	UserID      string `json:"user_id"`
	EmailFriend string `json:"email_friend"`
}

// input create Friend Request
type CreateFriendRequestInput struct {
	UserID      string `json:"user_id"`
	EmailFriend string `json:"email_friend"`
}

// input end friend request
type EndFriendRequestInput struct {
	UserID    string `json:"user_id"`
	RequestID string `json:"request_id"`
}

// input delete friend request
type DeleteFriendInput struct {
	UserID      string `json:"user_id"`
	FriendEmail string `json:"friend_email"`
}

// input accept friend request
type AcceptFriendRequestInput struct {
	UserAcceptID string `json:"user_accept_id"`
	RequestID    string `json:"request_id"`
}

// input reject friend request
type RejectFriendRequestInput struct {
	UserAcceptID string `json:"user_accept_id"`
	RequestID    string `json:"request_id"`
}

// output list friend request
type GetListFriendRequestOutput struct {
	RequestID     string `json:"request_id"`
	FromUser      string `json:"from_user"`
	UserNickName  string `json:"user_nickname"`
	UserAvatar    string `json:"user_avatar"`
	StatusRequest string `json:"status_request"`
	CreatedAt     string `json:"created_at"`
}

// input get friend request
type GetFriendRequestInput struct {
	UserID string `json:"user_id"`
	Limit  int    `json:"limit"`
	Page   int    `json:"page"`
}

// User Change Password input request
type UserChangePasswordInput struct {
	UserID      string `json:"user_id"`
	OldPassword string `json:"old_password"`
	NewPassword string `json:"new_password"`
}

// LogoutInput
type LogoutInput struct {
	UserID       string `json:"user_id"`
	AccessToken  string `json:"access_token"`
	RefreshToken string `json:"refresh_token"`
}

// RefreshTokenInput
type RefreshTokenInput struct {
	AccessToken  string `json:"access_token"`
	RefreshToken string `json:"refresh_token"`
}
