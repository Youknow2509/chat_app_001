package sendto

import (
	"encoding/json"
	"time"

	"example.com/be/internal/consts"
	"example.com/be/internal/utils/kafka/impl"
	"github.com/segmentio/kafka-go"
)

type kafka_send_mail struct {
}

// SendKafkaEmailOTP implements ISendTo.
func (k *kafka_send_mail) SendKafkaEmailOTP(from string, to string, type_send int, data string) error {
	body := make(map[string]interface{})

	body["from"] = from
	body["to"] = to
	body["type"] = type_send
	body["data"] = data
	// requestBody
	requestBody, _ := json.Marshal(body)
	
	// create message in kafaka
	msg := kafka.Message{
		Key:   []byte(consts.KEY_OTP_VERIFY),
		Value: []byte(requestBody),
		Time:  time.Now(),
	}

	return impl.NewProducerSendMailOtpImpl().WriteMessages(msg)
}

// implement ISendTo interface and create
func NewKafkaSendTo() ISendTo {
	return &kafka_send_mail{}
}
