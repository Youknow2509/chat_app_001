package initialize

import (
	"github.com/Youknow2509/cloudinary_manager/internal/global"
	"github.com/Youknow2509/cloudinary_manager/pkg/setting"
)

func InitVariableGlobal() {
	// init config system
	global.Config = &setting.Config{}


}