package initialize

import (
	"log"

	"github.com/gin-gonic/gin"
)

func Init() *gin.Engine{
	
	log.Println("Initializing application...")
	
	InitVariableGlobal()
	InitConfig()
	// InitDBMongo()
	InitCloudinary()
	InitRedis()

	r := InitRouter()

	return r
}