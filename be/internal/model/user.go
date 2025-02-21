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
	UserNickName string `json:"user_nickname"`
	UserAvatar   string `json:"user_avatar"`
}