package middleware

import (
	"github.com/Youknow2509/cloudinary_manager/internal/utils/auth"
	"github.com/gin-gonic/gin"
)

// handle auth token
func AuthMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		// get token from header
		token, ok := auth.GetAccessTokenFromHeader(c)
		if !ok {
			c.JSON(401, gin.H{
				"error": "Unauthorized",
			})
			c.Abort()
			return
		}
		// validate token
		claims, err := auth.ValidateTokenSubject(token)
		if err != nil {
			c.JSON(401, gin.H{
				"error": "Unauthorized",
			})
			c.Abort()
			return
		}
		// set data to context
		c.Set("user_id", claims.UserID)
	    c.Set("subject_id", claims.Subject)

		c.Next()
	}
}