package router

import (
	"example.com/be/internal/router/token"
	"example.com/be/internal/router/user"
)

type RouterGroup struct {
	User   user.UserRouterGroup
	Token  token.JwtTokenRouterGroup
}

var RouterGroupApp = new(RouterGroup)