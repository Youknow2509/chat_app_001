package middlewares

import (
	"context"
	"log"

	"example.com/be/internal/consts"
	"example.com/be/internal/utils/auth"
	"github.com/gin-gonic/gin"
)

// func authen middleware
func AuthenMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		// get the url in request
		url := c.Request.URL.Path
		log.Printf("Request URL: %s", url)
		// get headers authorization - get jwt token in header
		jwtToken, err := auth.ExtractBearerToken(c)
		if !err {
			c.AbortWithStatusJSON(401, gin.H{"code": 40001, "err": "Unauthorized", "description": "Get authorization header failed"})
			return
		}
		// validate token
		claims, ok := auth.ValidateTokenSubject(jwtToken)
		if ok != nil {
			c.AbortWithStatusJSON(401, gin.H{"code": 40002, "err": "Invalid token", "description": "Validate token failed"})
			return
		}
		// update claims to context
		log.Println("Claims:: uuid:: ", claims.Subject)
		// set data subject uuid to context
		ctx := context.WithValue(c.Request.Context(), consts.PAYLOAD_SUBJECT_UUID, claims.Subject)
		// set data user id to context
		ctx = context.WithValue(ctx, consts.PAYLOAD_USER_ID, claims.UserID)

		c.Request = c.Request.WithContext(ctx)
		c.Next()
	}
}