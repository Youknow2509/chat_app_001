package utils

import (
	"fmt"
	"strings"

	"github.com/google/uuid"
)

/**
 * Get key OTP 2FA Auth send
 */
func GetTwoFactorKeyVerify(key string) string {
	return fmt.Sprintf("u:%s:2fa:send", key)
}

/**
 * Get key OTP Two Factor Verify in cache 
 */
func GetTwoFactorKeyVerifyRegister(key string) string {
	return fmt.Sprintf("u:%s:2fa", key)
}

/**
 * Get key OTP verify user register in cache 
 */
func GetUserRegisterKeyVerify(key string) string {
	return fmt.Sprintf("u:%s:otp", key)
}

// create uuid
func GenerateCliTokenUUID(userId string) string {
	newUUID := uuid.New()
	// convert uuid to string, remove -
	uuidStr := strings.ReplaceAll(newUUID.String(), "-", "")
	return userId + ":clitoken:" + uuidStr
}

// get offset with limit and page number
func GetOffsetWithLimit(pageNumber, limit int) int {
	return (pageNumber - 1) * limit
}