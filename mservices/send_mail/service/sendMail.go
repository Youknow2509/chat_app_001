package service

import "example.com/send_mail/model"

// interface send mail service
type ISendMailService interface {
	SendMail(model.Message) error
}

var vISendMailService ISendMailService

// New ISendMailService creates a new ISendMailService
func NewSendMailService(sendMailImpl ISendMailService) {
	vISendMailService = sendMailImpl
}

// get service interface
func GetSendMailService() ISendMailService {
	if vISendMailService == nil {
		panic("SendMailService not initialized")
	}

	return vISendMailService
}