package impl

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"strings"

	"example.com/send_mail/global"
	"example.com/send_mail/model"
	"example.com/send_mail/service"

)

// struct ReaderKafkaSendOTPImpl
type ReaderKafkaSendOTPImpl struct {
}

// ReadMessageAndHandle implements service.IReaderKafka.
func (rki *ReaderKafkaSendOTPImpl) ReadMessageAndHandle(ctx context.Context) {
	r := global.ReaderOTPAuth
	for {
		m, err := r.ReadMessage(context.Background())
		if err != nil {
			log.Fatal("failed to read message:", err)
			break
		}
		
		key := strings.TrimSpace(string(m.Key))
		var value model.MessageMail
		if err := json.Unmarshal(m.Value, &value); err != nil {
			log.Fatal("failed to unmarshal message:", err)
			continue
		}

		// Process message here
        fmt.Printf("Consumed message: %s, Key: %s, Value: %+v\n", string(m.Topic), key, value)
		
		implServiceSendMail := NewSendMailImpl()
		service.NewSendMailService(implServiceSendMail)
		sSendMail := service.GetSendMailService()
		err = sSendMail.SendMailOTP(value)
		if err != nil {
			log.Fatal("failed to send mail:", err)
			continue
		}
	}
	
	if err := r.Close(); err != nil {
		log.Fatal("failed to close reader:", err)
	}
}

// init and implementation IReaderKafka
func NewReaderKafkaSendOTPImpl() service.IReaderKafka {
	return &ReaderKafkaSendOTPImpl{}
}
