package initialize

import (
	"log"

	r "github.com/Youknow2509/cloudinary_manager/internal/router"
	"github.com/gin-gonic/gin"
)

func InitRouter() *gin.Engine {
	var router *gin.Engine

	gin.SetMode(gin.DebugMode)
	gin.ForceConsoleColor()
	router = gin.Default()

	cloudinaryRouter := r.Routers.Cloudinary

	MainGroup := router.Group("/api")
	{
		cloudinaryRouter.InitCloudinaryRouter(MainGroup)
	}

	log.Println("Router initialized successfully")
	
	return router
}
