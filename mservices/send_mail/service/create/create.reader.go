package create

import (
	"example.com/send_mail/global"
	"example.com/send_mail/service"
	"example.com/send_mail/service/impl"
)

// factory create reader
func FactoryCreateReader(type_reader string) service.IReaderKafka {
	switch type_reader {
	case global.Config.Kafka.TopicMailNewPassword:
		return impl.NewReaderKafkaNewPassWordImpl()
	case global.Config.Kafka.TopicNotifyUser:
		return impl.NewReaderKafkaNotifyUserImpl()
	case global.Config.Kafka.TopicMailOTP:
		return impl.NewReaderKafkaSendOTPImpl()
	default:
		return nil
	}
}