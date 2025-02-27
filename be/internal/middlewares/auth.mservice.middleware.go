package middlewares

import (
	"log"

	"example.com/be/internal/utils/auth"
	"github.com/gin-gonic/gin"
)

// func authen middleware
func AuthenMServiceMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		// get the url in request
		url := c.Request.URL.Path
		log.Printf("Request URL: %s", url)
		// get headers authorization - get token in header
		token, err := auth.ExtractBearerToken(c)
		if !err {
			c.AbortWithStatusJSON(401, gin.H{"code": 40001, "err": "Unauthorized", "description": "Get authorization header failed"})
			return
		}
		// validate token
		// TODO: handle authorization service
		if token != "123456" {
			c.AbortWithStatusJSON(401, gin.H{"code": 40002, "err": "Invalid token", "description": "Validate token failed"})
			return
		}		

		c.Next()
	}
}