package auth

import (
	"strings"

	"example.com/be/global"
	"github.com/gin-gonic/gin"
)

// Extract the token from the header
func ExtractBearerToken(c *gin.Context) (string, bool) {
	// Authorization: Bearer token
	authHeader := c.GetHeader("Authorization")
	if strings.HasPrefix(authHeader, "Bearer ") {
		return strings.TrimPrefix(authHeader, "Bearer "), true
	}
	return "", false
}

// check token exists in cache
func CheckAccessTokenExists(ctx *gin.Context , subUUID string) error {
	err := global.Rdb.Get(ctx, subUUID).Err()
	if err != nil {

		return err
	}
	return nil
}