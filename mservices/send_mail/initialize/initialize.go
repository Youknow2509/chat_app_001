package initialize

import (
	"fmt"

	"example.com/send_mail/consts"
	"example.com/send_mail/global"
	"example.com/send_mail/utils/kafka_reader"
)

// func initialize
func Initialize() {
	// init reader mail kafaka
	global.ReaderOTPAuth = kafkareader.GetKafkaReader(
		consts.TOPIC_GO_SEND_MAIL_OTP, 
		global.Config.Kafka.BootstraperSeverMail, 
		"gr-01",
	)

	fmt.Println("Initialize Service Reader Mail From Kafka Server is running")
}