package global

import (
	"example.com/send_mail/setting/config"
	"github.com/segmentio/kafka-go"
)

var (
	ReaderOTPAuth *kafka.Reader
	Config        config.Config
)
