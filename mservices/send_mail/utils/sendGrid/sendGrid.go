package sendGrid

var vISendMail ISendGridMail


// Send a simple text email
type EmailAddress struct {
	Address string `json:"address"`
	Name    string `json:"name"`
}

// Send a simple text email
type Mail struct {
	From             EmailAddress `json:"from"`
	To               string       `json:"to"`
	Subject          string       `json:"subject"`
	Body             string       `json:"body"`
	PlainTextContent string       `json:"plainTextContent"`
	HtmlContent      string       `json:"htmlContent"`
}

// New interface for sending mail
func NewISendMail(iSendMail ISendGridMail) {
	vISendMail = iSendMail
}

// interface for sending mail
type ISendGridMail interface {
	SendText(from, to, data string) error
	SendTemplateEmailOTP(from, to, data string) error
}

// get instance of ISendMail
func GetISendMail() ISendGridMail {
	if (vISendMail == nil) {
		panic("SendGrid not initialized")
	}
	return vISendMail
}

