package router

import "example.com/be/internal/router/user"

type RouterGroup struct {
	User   user.UserRouterGroup
}

var RouterGroupApp = new(RouterGroup)