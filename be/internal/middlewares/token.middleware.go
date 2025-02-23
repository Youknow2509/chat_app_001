package middlewares

import (
	"example.com/be/global"
	"example.com/be/internal/utils/auth"
	ctxUtil "example.com/be/internal/utils/context"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

// func token middleware
func TokenAuthMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		// get subject uuid from header
		subUUID, err := ctxUtil.GetSubjectUUID(c.Request.Context())
		if err != nil {
			global.Logger.Error("GetSubjectUUID error: ", zap.Error(err))
			c.JSON(401, gin.H{
				"message": "Unauthorized",
			})
			c.Abort()
			return
		}
		// check token in cache
		err = auth.CheckAccessTokenExists(c, subUUID)
		if err != nil {
			global.Logger.Error("CheckAccessTokenExists error: ", zap.Error(err))
			c.JSON(401, gin.H{
				"message": "Unauthorized",
			})
			c.Abort()
			return
		}
		
		c.Next()
	}
}