package model

import "time"

// UserInfoOutput
type UserInfoOutput struct {
	UserID       string    `json:"user_id"`
	UserAccount  string    `json:"user_account"`
	UserNickname string    `json:"user_nickname"`
	UserAvatar   string    `json:"user_avatar"`
	UserState    string    `json:"user_state"`
	UserMobile   string    `json:"user_mobile"`
	UserGender   string    `json: "user_gender"`
	UserBirthday time.Time `json:"user_birthday"`
	UserEmail    string    `json:"user_email"`
	CreatedAt    time.Time `json:"created_at"`
	UpdatedAt    time.Time `json:"updated_at"`
}

// UpdateUserInfoInput
type UpdateUserInfoInput struct {
	UserID       string `json:"user_id"`
	UserNickName string `json:"user_nickname"`
	UserAvatar   string `json:"user_avatar"`
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
	ToUser        string `json:"to_user"`
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
