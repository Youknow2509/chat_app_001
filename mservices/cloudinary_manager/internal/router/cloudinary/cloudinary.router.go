package cloudinary

import (
	"github.com/Youknow2509/cloudinary_manager/internal/controller"
	"github.com/Youknow2509/cloudinary_manager/internal/middleware"
	"github.com/gin-gonic/gin"
)

type ClouDinaryRouter struct {

}

// init cloudinary router group
func (c *ClouDinaryRouter) InitCloudinaryRouter(rg *gin.RouterGroup) {
	privateRouter := rg.Group("/v1")
	privateRouter.Use(middleware.AuthMiddleware())
	{
		privateRouter.POST("/getSignature", controller.GetCloudinaryController().GetSignature)
		privateRouter.GET("/getMedia")
		privateRouter.POST("/uploadMedia")
		privateRouter.POST("/deleteMedia")
	}
}