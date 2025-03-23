package model

// Message Mail
type MessageMail struct {
	From string `json:"from"`
	To   string `json:"to"`
	Data string `json:"data"`
	Type int    `json:"type"`
}

// Message Mail Forgot Password
type MessageMailForgotPassword struct {
	From        string `json:"from"`
	To          string `json:"to"`
	Type        int    `json:"type"`
	EndPoint    string `json:"endpoint"`
	PasswordNew string `json:"passwordNew"`
}

// Message Notification
type MessageNotification struct {
	Body            string `json:"body"`
	SenderAvatarURL string `json:"sender_avatar_url"`
	Title           string `json:"title"`
	UserIdTarget    string `json:"user_id_target"`
}
