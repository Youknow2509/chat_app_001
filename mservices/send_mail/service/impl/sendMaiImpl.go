package impl

import (
	"fmt"

	"example.com/send_mail/model"
	"example.com/send_mail/service"
	uSendgrid "example.com/send_mail/utils/sendGrid"
	uiSendgrid "example.com/send_mail/utils/sendGrid/impl"
)

type SendMailImpl struct{}

// SendMail implements service.ISendMailService.
func (s *SendMailImpl) SendMail(message model.Message) error {
	// init send grid impl
	sendGridImpl := uiSendgrid.NewSendGridImpl()
	// init interface for send grid
	uSendgrid.NewISendMail(sendGridImpl)
	uSendmail := uSendgrid.GetISendMail()
	// send mail
    // err := uSendmail.SendText(message.From, message.To, message.Data)
    err := uSendmail.SendTemplateEmailOTP(message.From, message.To, message.Data) 
	if err != nil {
        fmt.Errorf("Send mail failed: %v", err)
        return err
    } 
	return nil
}

// new and impl interface for SendMail
func NewSendMailImpl() service.ISendMailService {
	return &SendMailImpl{}
}
