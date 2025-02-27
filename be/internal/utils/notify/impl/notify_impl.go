package impl

import (
	"encoding/json"
	"time"

	"example.com/be/global"
	"example.com/be/internal/consts"
	"example.com/be/internal/utils/notify"
	"github.com/segmentio/kafka-go"
	"go.uber.org/zap"
	kafakaServiceImpl "example.com/be/internal/utils/kafka/impl"
)

// struct notify implements
type NotifyImpl struct {
}

// get structor implementation
func GetNotifyImpl() notify.INotify {
	return &NotifyImpl{}
}

// SendKafkaNotificationUser implements notify.INotify.
func (n *NotifyImpl) SendKafkaNotificationUser(userIdTarget string, body interface{}, title string, senderAvatarURL string) error {
	
	bodyKafka := make(map[string]interface{})

	bodyKafka["UserIdTarget"] = userIdTarget
	bodyKafka["Body"] = body
	bodyKafka["Title"] = title
	bodyKafka["SenderAvatarURL"] = senderAvatarURL

	// requestBody
	requestBody, err := json.Marshal(bodyKafka)
	if err != nil {
		global.Logger.Error("Error when marshal bodyKafka notify ::", zap.Error(err))
		return err
    }
	// create message in kafaka
	msg := kafka.Message{
		Key:   []byte(consts.KEY_KAFKA_NOTIFY),
		Value: []byte(requestBody),
		Time:  time.Now(),
	}

	return kafakaServiceImpl.NewProducerNotifyUserImpl().WriteMessages(msg)
}