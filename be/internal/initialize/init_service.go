package initialize

import (
	"example.com/be/global"
	"example.com/be/internal/database"
	"example.com/be/internal/service"
	"example.com/be/internal/service/impl"
)

// initialize services
func InitServiceInterface() {
	q := database.New(global.Mdbc)
	service.InitUserLogin(impl.NewSUserLogin(q))

	// ...
}
