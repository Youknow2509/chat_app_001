package utils

import (
	"context"
	"fmt"
	"strings"

	"example.com/be/global"
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

// delete cache with prefix 
func DeleteCacheWithKeyPrefix(prefix string) error {
	ctx := context.Background()
	prefix = fmt.Sprintf("%s*", prefix)
	// scan the prefix
	iter := global.Rdb.Scan(ctx, 0, prefix, 0).Iterator()
	for iter.Next(ctx) {
		err := global.Rdb.Del(ctx, iter.Val()).Err()
		if err != nil {
			fmt.Println("Error delete key: ", iter.Val())
			return err
		} else {
			fmt.Println("Delete key: ", iter.Val())
		}
	}
	if err := iter.Err(); err != nil {
		fmt.Println("Error iterating keys:", err)
		return err
	}

    return nil
}