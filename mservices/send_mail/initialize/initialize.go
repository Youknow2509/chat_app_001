package initialize

import (
	"fmt"
	"example.com/send_mail/global"
	"example.com/send_mail/utils/kafka_reader"
)

// func initialize
func Initialize() {
	// init reader mail otp kafaka
	global.ReaderOTPAuth = kafkareader.GetKafkaReader(
		global.Config.Kafka.TopicMailOTP, 
		global.Config.Kafka.BootstraperSeverMail, 
		"gr-01",
	)

	fmt.Println("Initialize Service Reader Mail From Kafka Server is running")

	// init reader mail new password kafaka
	global.ReaderNewPassword = kafkareader.GetKafkaReader(
		global.Config.Kafka.TopicMailNewPassword,
		global.Config.Kafka.BootstraperSeverMail,
		"gr-02",
	)

	// init reader notify user kafaka
	global.ReaderNotifyUser = kafkareader.GetKafkaReader(
		global.Config.Kafka.TopicNotifyUser,
		global.Config.Kafka.BootstraperSeverMail,
		"gr-03",
	)
}