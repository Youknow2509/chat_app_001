package initialize

import (
	"context"
	"sync"

	"example.com/send_mail/global"
	"example.com/send_mail/service/create"
)

// reader and process
func ReaderAndProcess(wg *sync.WaitGroup) {
	defer wg.Done()
	readerSendOTP := create.FactoryCreateReader(global.Config.Kafka.TopicMailOTP)
	go readerSendOTP.ReadMessageAndHandle(context.Background())

	readerSendNotification := create.FactoryCreateReader(global.Config.Kafka.TopicMailNewPassword)
	go readerSendNotification.ReadMessageAndHandle(context.Background())
}
