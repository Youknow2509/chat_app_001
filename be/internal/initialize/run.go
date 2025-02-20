package initialize

import (
	"fmt"

	"example.com/be/global"
	"github.com/gin-gonic/gin"
)

// Run all initialization
func Run() *gin.Engine {
	// load configuration
	LoadConfig()
	fmt.Println("@@@ Loader configuration")

	// initialize logger
	InitLogger()
	global.Logger.Info("Logger initialized")

	// initialize mysql
	// connect to my sql
	InitMysql()
	global.Logger.Info("Mysql initialized")

	// innitialize sqlc
	InitMysqlC()
	global.Logger.Info("MysqlC initialized")

	// initialize service interface
	InitServiceInterface()
	global.Logger.Info("Service interface initialized")

	// connect to redis
	InitRedis()
	global.Logger.Info("Redis initialized")

	// connect to Router
	r := InitRouter()

	return r
}
