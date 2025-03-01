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
	nameMailTemplate = "otp-auth.html"
)

// struct
type SendGridImpl struct {
}

// SendTemplateEmailOTP implements sendgrid.ISendMail.
func (s *SendGridImpl) SendTemplateEmailOTP(from string, to string, data string) error {
	// create data from template
	dataTemplate := map[string]interface{}{
		"otp": data,
	}
	// create mail html template
	mailTemplateHtml, err := getMailTemplate(nameMailTemplate, dataTemplate)
	if err != nil {
		return err
	}
	// create mail
	mail := &isendgrid.Mail{
		From: isendgrid.EmailAddress{
			Address: from,
			Name:    "Ly Tran Vinh",
		},
		To:               to,
		Subject:          "OTP Verification",
		PlainTextContent: "",
		HtmlContent:      mailTemplateHtml,
	}
	// send mail
	err = sendMail(*mail)
	if err != nil {
		return err
	}

	return nil
}

// SendText implements sendgrid.ISendMail.
func (s *SendGridImpl) SendText(from string, to string, data string) error {
	mail := &isendgrid.Mail{
		From: isendgrid.EmailAddress{
			Address: from,
			Name:    "Ly Tran Vinh",
		},
		To:               to,
		Subject:          "OTP Verification",
		PlainTextContent: fmt.Sprintf("Your OTP is: %s, Please enter it to verify your account.", data),
		HtmlContent:      "",
	}

	err := sendMail(*mail)
	if err != nil {
		return err
	}

	return nil
}

// new SendGridImpl
func NewSendGridImpl() isendgrid.ISendGridMail {
	return &SendGridImpl{}
}

// Help get template email html
func getMailTemplate(
	nameMailTemplate string,
	dataTemplate map[string]interface{},
) (string, error) {
	htmlTemplate := new(bytes.Buffer)

	t := template.Must(
		template.New(nameMailTemplate).ParseFiles("html-template/mail/" + nameMailTemplate))

	err := t.Execute(htmlTemplate, dataTemplate)
	if err != nil {
		return "", err
	}
	return htmlTemplate.String(), nil
}

// Build message in SendGrid
func BuildMessageInSendGird(m isendgrid.Mail) *mail.SGMailV3 {
	from := mail.NewEmail(m.From.Name, m.From.Address)

	subject := m.Subject
	to := mail.NewEmail("", m.To)
	plainTextContent := m.PlainTextContent
	htmlContent := m.HtmlContent

	return mail.NewSingleEmail(
		from,
		subject,
		to,
		plainTextContent,
		htmlContent,
	)
}

// help send
func sendMail(m isendgrid.Mail) error {
	message := BuildMessageInSendGird(m)

	client := sendgrid.NewSendClient(global.Config.SendGrid.APIKey)

	response, err := client.Send(message)
	if err != nil {
		log.Println("Error sending email: ", err)
		return err
	}

	// check status code
	if response.StatusCode != 202 {
		log.Println("Email send failed:: ", response)
		return fmt.Errorf("Email send failed:: %v", response)
	}

	return nil
}
