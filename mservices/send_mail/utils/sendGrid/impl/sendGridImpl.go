package impl

import (
	"bytes"
	"fmt"
	"html/template"
	"log"
	"time"

	"example.com/send_mail/consts"
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

func (s *SendGridImpl) SendTemplateEmailNewPasswork(from, to, token, password string) error {
	mailTemplateHtml, err := getMailTemplate(
		nameMailNewPasswordTemplate, 
		map[string]interface{}{
			"userName": to,
			"resetPasswordLink": 
				consts.APP_URL + "/v1/user/verify_forgot_password/" + to + "/" + token, 
			"temporaryPassword": password,
			"userEmail": to,
			"privacyPolicyUrl": "https://www.freeprivacypolicy.com/live/051ebb07-d8a6-490c-a003-58bc332234d0",
			"termsOfServiceUrl": "https://www.freeprivacypolicy.com/live/1fa8c319-1480-402f-ac5d-5eb9261db25e",
			"date": time.Now().UTC().Format(time.RFC1123),
		},
	)
	if err != nil {
		return err
	}

	return sendMail(isendgrid.Mail{
		From:        isendgrid.EmailAddress{Address: from, Name: "Ly Tran Vinh"},
		To:          to,
		Subject:     "Verify forgot password",
		HtmlContent: mailTemplateHtml,
	})
}

func (s *SendGridImpl) SendTemplateEmailOTP(from, to, data string) error {
	mailTemplateHtml, err := getMailTemplate(
		nameMailOTPTemplate, 
		map[string]interface{}{
			"userName": to,
			"otpCode": data,
			"verificationLink": consts.APP_URL + "/v1/user/verify_account?verify_code=" + data + "&verify_key=" + to,
			"userEmail": to,
			"privacyPolicyUrl": "https://www.freeprivacypolicy.com/live/051ebb07-d8a6-490c-a003-58bc332234d0",
			"termsOfServiceUrl": "https://www.freeprivacypolicy.com/live/1fa8c319-1480-402f-ac5d-5eb9261db25e",
		},
	)
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
