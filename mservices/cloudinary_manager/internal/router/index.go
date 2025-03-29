package router

import "github.com/Youknow2509/cloudinary_manager/internal/router/cloudinary"

type RouterApp struct {
	Cloudinary cloudinary.ClouDinaryRouterGroup
}

var Routers = new(RouterApp)