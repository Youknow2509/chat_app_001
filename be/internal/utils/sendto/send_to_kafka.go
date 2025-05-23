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

// SendKafkaMailNewPassword implements ISendTo.
func (k *kafka_send_mail) SendKafkaMailNewPassword(from string, to string, type_send int, endpoint string, passwordNew string) error {
	body := make(map[string]interface{})

	body["from"] = from
	body["to"] = to
	body["type"] = type_send
	body["endpoint"] = endpoint
	body["passwordNew"] = passwordNew
	
	// requestBody
	requestBody, _ := json.Marshal(body)

	// create message in kafaka
	msg := kafka.Message{
		Key:   []byte(consts.KEY_NEW_PASSWORD_OTP),
		Value: []byte(requestBody),
		Time:  time.Now(),
	}

	return impl.NewProducerSendMailNewPasswordImpl().WriteMessages(msg)
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
