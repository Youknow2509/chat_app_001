package initialize

import (
	"example.com/be/global"
	"example.com/be/pkg/logger"
)

// Initial Logger
func InitLogger() {
	global.Logger = logger.NewLogger(global.Config.Logger)
}
