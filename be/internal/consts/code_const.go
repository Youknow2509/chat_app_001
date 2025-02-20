package consts

const (
	EMAIL int = 1
	MOBILE int = 2

	TIME_OTP_REGISTER int = 1
	TIME_2FA_OTP_REGISTER int = 10

	EMAIL_HOST = "lytranvinh.work@gmail.com"

	TWO_FACTOR_EMAIL = "EMAIL"
	TWO_FACTOR_SMS = "SMS"
	
	PAYLOAD_SUBJECT_UUID = "SUBJECT_UUID"

	SEND_EMAIL_OTP = 1

)


// kafka
const (
	KEY_OTP_VERIFY = "otp_verify"

	TOPIC_SERVICE_SEND_MAIL_OTP = "go-service-send-mail-otp"
	TCP_KAFKA = "localhost:9094"
)