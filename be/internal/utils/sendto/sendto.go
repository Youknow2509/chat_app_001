package sendto

import (
)

// Enum Type for sending
const (
	TYPE_KAFKA = "kafka"
)

// interface for sending email
type ISendTo interface {
	// Send to kafka handler service send mail
	SendKafkaEmailOTP(from string, to string, type_send int, data string) error
	SendKafkaMailNewPassword(from string, to string, type_send int, endpoint string, passwordNew string) error
}

