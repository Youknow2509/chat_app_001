package impl

import (
	"fmt"

	"example.com/send_mail/model"
	"example.com/send_mail/service"
	uSendgrid "example.com/send_mail/utils/sendGrid"
	uiSendgrid "example.com/send_mail/utils/sendGrid/impl"
)

type SendMailImpl struct{}

func (s *SendMailImpl) SendMailNewPassword(message model.MessageMailForgotPassword) error {
	uSendmail := getSendGridInstance()
	if err := uSendmail.SendTemplateEmailNewPasswork(message.From, message.To, message.EndPoint, message.PasswordNew); err != nil {
		return fmt.Errorf("Send mail failed: %v", err)
	}
	return nil
}

func (s *SendMailImpl) SendMailOTP(message model.MessageMail) error {
	uSendmail := getSendGridInstance()
	if err := uSendmail.SendTemplateEmailOTP(message.From, message.To, message.Data); err != nil {
		return fmt.Errorf("Send mail failed: %v", err)
	}
	return nil
}

func NewSendMailImpl() service.ISendMailService {
	return &SendMailImpl{}
}

func getSendGridInstance() uSendgrid.ISendGridMail {
	sendGridImpl := uiSendgrid.NewSendGridImpl()
	uSendgrid.NewISendMail(sendGridImpl)
	return uSendgrid.GetISendMail()
}
