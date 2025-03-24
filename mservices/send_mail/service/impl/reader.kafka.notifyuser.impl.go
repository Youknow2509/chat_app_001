package impl

import (
	"context"
	"example.com/send_mail/service"
)

// struct ReaderKafkaNotifyUserImpl
type ReaderKafkaNotifyUserImpl struct {
}

// ReadMessageAndHandle implements service.IReaderKafka.
func (rki *ReaderKafkaNotifyUserImpl) ReadMessageAndHandle(ctx context.Context) {
	panic("implement me")
}

// init and implementation IReaderKafka
func NewReaderKafkaNotifyUserImpl() service.IReaderKafka {
	return &ReaderKafkaNotifyUserImpl{}
}
