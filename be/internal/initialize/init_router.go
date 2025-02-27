package initialize

import (
	"example.com/be/global"
	routers "example.com/be/internal/router"
	"github.com/gin-gonic/gin"
)

// Initial router
func InitRouter() *gin.Engine {
	// router := gin.Default()
	var router *gin.Engine

	if global.Config.Server.Mode == "dev" {
		gin.SetMode(gin.DebugMode)
		gin.ForceConsoleColor()
		router = gin.Default()
	} else {
		gin.SetMode(gin.ReleaseMode)
		router = gin.New()
	}

	userRouter := routers.RouterGroupApp.User
	tokenRouter := routers.RouterGroupApp.Token
	chatRouter := routers.RouterGroupApp.Chat
	mServiceRouter := routers.RouterGroupApp.MService

	MainGroup := router.Group("/v1")
	{
		MainGroup.GET("/checkStatus") // tracking monitor 
	}
	{
		userRouter.InitUserRouter(MainGroup)
		tokenRouter.InitTokenRouter(MainGroup)
		chatRouter.InitChatBaseRouter(MainGroup)
		mServiceRouter.InitChatServiceRouter(MainGroup)
        //... other routes...
	}

	return router
}