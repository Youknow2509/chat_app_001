package notify

// interface notify
type INotify interface {
	SendKafkaNotificationUser(
		userIdTarget string, 
		body interface{},
		title string,
		senderAvatarURL string,
	) error
}

var vINotify INotify

// init interface
func InitINotify(i INotify) {
	vINotify = i
}

// get interface
func GetINotify() INotify {
	if vINotify == nil {
        panic("Notify interface is not initialized")
    }
    return vINotify
}