package model

// create chat group input
type CreateChatGroupInput struct {
	GroupName string   `json:"group_name"`
	ListId    []string `json:"list_id"`
}

// CreateChatPrivateInput
type CreateChatPrivateInput struct {
	User1 string `json:"user1"`
	User2 string `json:"user2"`
}

// Chat information
type ChatInfoOutput struct {
	ChatName       string   `json:"chat_name"`
	NumberofMember int      `json:"number_of_member"`
	ListId         []string `json:"list_id"`
	TypeChat       string   `json:"type_chat"`
}

// GetUserInChat output
type GetUserInChatOutput struct {
	ChatID     string   `json:"chat_id"`
	ListUserID []string `json:"list_user_id"`
}

// AddMemberToChat input
type AddMemberToChatInput struct {
	AdminChatID string `json:"admin_chat_id"`
	ChatID      string `json:"chat_id"`
	UserAddID   string `json:"user_add_id"`
}

// UpgradeChatInfo input
type UpgradeChatInfoInput struct {
	GroupChatID     string `json:"group_chat_id"`
	GroupNameUpdate string `json:"group_name_update"`
	GroupAvatar     string `json:"group_avatar"`
}

// ChangeAdminGroupChat input
type ChangeAdminGroupChatInput struct {
	GroupChatID string `json:"group_chat_id"`
	NewAdminID  string `json:"new_admin_id"`
	OldAdminID  string `json:"old_admin_id"`
}

// DelMenForChat input
type DelMenForChatInput struct {
	AdminChatID string `json:"admin_chat_id"`
	ChatID      string `json:"chat_id"`
	UserDelID   string `json:"user_del_id"`
}

// DelChat input
type DelChatInput struct {
	AdminChatID string `json:"admin_chat_id"`
	ChatID      string `json:"chat_id"`
}