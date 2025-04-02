package consts

const (
	EMAIL int = 1
	MOBILE int = 2

	TIME_OTP_REGISTER int = 1
	TIME_2FA_OTP_REGISTER int = 10
	TIME_SAVE_CACHE_OFTEN_USE int = 10
	TIME_BLOCK_CREATE_FRIEND_REQUEST int = 7 // 7h
	TIME_BLOCK_FORGOT_PASSWORD_REQUEST int = 12 // 12h
	TIME_EX_UPDATE_NAME_REGISTER int = 1 // 1h

	EMAIL_HOST = "lytranvinh.work@gmail.com"

	TWO_FACTOR_EMAIL = "EMAIL"
	TWO_FACTOR_SMS = "SMS"
	
	PAYLOAD_SUBJECT_UUID = "SUBJECT_UUID"
	PAYLOAD_USER_ID = "USER_ID"

	SEND_EMAIL_OTP = 1

)


// kafka
const (
	KEY_OTP_VERIFY = "otp_verify"
	KEY_KAFKA_NOTIFY = "device_notify"
	KEY_NEW_PASSWORD_OTP = "new_password_user"

	TOPIC_SERVICE_SEND_MAIL_OTP = "go-service-send-mail-otp"
	TOPIC_SERVICE_NOTIFICATION = "user-request-notification"
	TOPIC_SERVICE_SEND_NEW_PASSWORD = "go-service-send-mail-new-password"
)
var TCP_KAFKA = "localhost:9094"