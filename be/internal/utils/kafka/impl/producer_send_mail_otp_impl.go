package impl

import (
	"context"

	"example.com/be/internal/consts"
	iKafka "example.com/be/internal/utils/kafka"
	kafka "github.com/segmentio/kafka-go"
)

var producer_send_mail_otp *kafka.Writer

type producer_send_mail_otp_impl struct {
}

// Close implements kafka.IKafkaProducer.
func (p *producer_send_mail_otp_impl) Close() error {
	panic("unimplemented")
}

// WriteMessages implements kafka.IKafkaProducer.
func (p *producer_send_mail_otp_impl) WriteMessages(messages ...kafka.Message) error {
	return producer_send_mail_otp.WriteMessages(context.Background(), messages...)
}

// new producer_send_mail_otp_impl
func NewProducerSendMailOtpImpl() iKafka.IKafkaProducer {
	producer_send_mail_otp = &kafka.Writer{
		Addr:     kafka.TCP(consts.TCP_KAFKA),
		Topic:    consts.TOPIC_SERVICE_SEND_MAIL_OTP,
		Balancer: &kafka.LeastBytes{},
	}

	return &producer_send_mail_otp_impl{}
}
