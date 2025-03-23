package impl

import (
	"context"
	"encoding/json"
	"fmt"
	"strings"

	"example.com/send_mail/global"
	"example.com/send_mail/model"
	"example.com/send_mail/service"
)

// struct ReaderKafkaNewPassWordImpl
type ReaderKafkaNewPassWordImpl struct {
}

// ReadMessageAndHandle implements service.IReaderKafka.
func (rki *ReaderKafkaNewPassWordImpl) ReadMessageAndHandle(ctx context.Context) {
	r := global.ReaderNewPassword
	for {
		m, err := r.ReadMessage(context.Background())
		if err != nil {
			fmt.Println("failed to read message:", err)
			break
		}
		
		key := strings.TrimSpace(string(m.Key))
		var value model.MessageMailForgotPassword
		if err := json.Unmarshal(m.Value, &value); err != nil {
			fmt.Println("failed to unmarshal message:", err)
			continue
		}

		// Process message here
        fmt.Printf("Consumed message: %s, Key: %s, Value: %+v\n", string(m.Topic), key, value)
		
		implServiceSendMail := NewSendMailImpl()
		service.NewSendMailService(implServiceSendMail)
		sSendMail := service.GetSendMailService()
		err = sSendMail.SendMailNewPassword(value)
		if err != nil {
			fmt.Println("failed to send mail:", err)
			continue
		}
	}
	
	if err := r.Close(); err != nil {
		fmt.Println("failed to close reader:", err)
	}
}

// init and implementation IReaderKafka
func NewReaderKafkaNewPassWordImpl() service.IReaderKafka {
	return &ReaderKafkaNewPassWordImpl{}
}
