package middlewares

import (
	"context"
	"database/sql"
	"log"

	"example.com/be/internal/consts"
	"example.com/be/internal/utils/auth"
	"example.com/be/internal/utils/cache"
	"github.com/gin-gonic/gin"
)

type InfoUserUUID struct {
	UserId             string
	UserAccount        string
	UserState          string
	UserIsRefreshToken sql.NullInt32
}

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
		// check token exist in cache - get infoUser Redis from uuid
		var userInfo InfoUserUUID
		if err := cache.GetCache(context.Background(), claims.Subject, &userInfo); err != nil {
			c.AbortWithStatusJSON(401, gin.H{"code": 40003, "err": "Unauthorized", "description": "Token not exist in cache"})
			return
		}
		// set data to context
		ctx := context.WithValue(c.Request.Context(), consts.PAYLOAD_SUBJECT_UUID, claims.Subject)
		c.Request = c.Request.WithContext(ctx)
		c.Next()
	}
}