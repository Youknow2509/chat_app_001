package impl

import (
	"context"

	"example.com/be/internal/consts"
	iKafka "example.com/be/internal/utils/kafka"
	kafka "github.com/segmentio/kafka-go"
)

var producer_notify_user *kafka.Writer

type producer_notify_user_impl struct {
}

// Close implements kafka.IKafkaProducer.
func (p *producer_notify_user_impl) Close() error {
	panic("unimplemented")
}

// WriteMessages implements kafka.IKafkaProducer.
func (p *producer_notify_user_impl) WriteMessages(messages ...kafka.Message) error {
	return producer_notify_user.WriteMessages(context.Background(), messages...)
}

// new producer_notify_user_impl
func NewProducerNotifyUserImpl() iKafka.IKafkaProducer {
	producer_notify_user = &kafka.Writer{
		Addr:     kafka.TCP(consts.TCP_KAFKA),
		Topic:    consts.TOPIC_SERVICE_NOTIFICATION,
		Balancer: &kafka.LeastBytes{},
	}

	return &producer_notify_user_impl{}
}
