package impl

import (
	"bytes"
	"fmt"
	"html/template"
	"log"

	"example.com/send_mail/global"
	isendgrid "example.com/send_mail/utils/sendGrid"
	"github.com/sendgrid/sendgrid-go"
	"github.com/sendgrid/sendgrid-go/helpers/mail"
)

const (
	nameMailOTPTemplate         = "otp-auth.html"
	nameMailNewPasswordTemplate = "auth-new-password.html"
)

type SendGridImpl struct{}

func (s *SendGridImpl) SendTemplateEmailNewPasswork(from, to, data string) error {
	mailTemplateHtml, err := getMailTemplate(nameMailNewPasswordTemplate, map[string]interface{}{"new_password": data})
	if err != nil {
		return err
	}

	return sendMail(isendgrid.Mail{
		From:        isendgrid.EmailAddress{Address: from, Name: "Ly Tran Vinh"},
		To:          to,
		Subject:     "New Password",
		HtmlContent: mailTemplateHtml,
	})
}

func (s *SendGridImpl) SendTemplateEmailOTP(from, to, data string) error {
	mailTemplateHtml, err := getMailTemplate(nameMailOTPTemplate, map[string]interface{}{"otp": data})
	if err != nil {
		return err
	}

	return sendMail(isendgrid.Mail{
		From:        isendgrid.EmailAddress{Address: from, Name: "Ly Tran Vinh"},
		To:          to,
		Subject:     "OTP Verification",
		HtmlContent: mailTemplateHtml,
	})
}

func (s *SendGridImpl) SendText(from, to, data string) error {
	return sendMail(isendgrid.Mail{
		From:             isendgrid.EmailAddress{Address: from, Name: "Ly Tran Vinh"},
		To:               to,
		Subject:          "OTP Verification",
		PlainTextContent: fmt.Sprintf("Your OTP is: %s, Please enter it to verify your account.", data),
	})
}

func NewSendGridImpl() isendgrid.ISendGridMail {
	return &SendGridImpl{}
}

func getMailTemplate(templateName string, data map[string]interface{}) (string, error) {
	htmlTemplate := new(bytes.Buffer)
	t, err := template.New(templateName).ParseFiles("html-template/mail/" + templateName)
	if err != nil {
		return "", err
	}

	if err := t.Execute(htmlTemplate, data); err != nil {
		return "", err
	}
	return htmlTemplate.String(), nil
}

func BuildMessageInSendGird(m isendgrid.Mail) *mail.SGMailV3 {
	return mail.NewSingleEmail(
		mail.NewEmail(m.From.Name, m.From.Address),
		m.Subject,
		mail.NewEmail("", m.To),
		m.PlainTextContent,
		m.HtmlContent,
	)
}

func sendMail(m isendgrid.Mail) error {
	client := sendgrid.NewSendClient(global.Config.SendGrid.APIKey)
	response, err := client.Send(BuildMessageInSendGird(m))
	if err != nil {
		log.Println("Error sending email: ", err)
		return err
	}

	if response.StatusCode != 202 {
		log.Println("Email send failed:: ", response)
		return fmt.Errorf("Email send failed:: %v", response)
	}
	return nil
}
