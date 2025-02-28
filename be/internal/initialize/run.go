package initialize

import (
	"fmt"

	"example.com/be/global"
	"example.com/be/internal/consts"
	"github.com/gin-gonic/gin"
)

// Run all initialization
func Run() *gin.Engine {
	// load configuration
	// LoadConfig() // Enable when dev
	LoadConfigProd() // Enable when deloy 
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

	// fix kafka TCP
	consts.TCP_KAFKA = fmt.Sprintf("%s:%d", global.Config.Kafka.Host, global.Config.Kafka.PortExternal)

	// connect to Router
	r := InitRouter()

	return r
}
