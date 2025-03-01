package initialize

import (
	"context"

	"example.com/send_mail/global"
	"example.com/send_mail/service/create"
)

// reader and process
func ReaderAndProcess() {
	readerSendOTP := create.FactoryCreateReader(global.Config.Kafka.TopicMailOTP)
	go readerSendOTP.ReadMessageAndHandle(context.Background())

	readerSendNotification := create.FactoryCreateReader(global.Config.Kafka.TopicMailNewPassword)
	go readerSendNotification.ReadMessageAndHandle(context.Background())
}
