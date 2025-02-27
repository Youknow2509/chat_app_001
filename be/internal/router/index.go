package router

import (
	"example.com/be/internal/router/chat"
	"example.com/be/internal/router/mservice"
	"example.com/be/internal/router/token"
	"example.com/be/internal/router/user"
)

type RouterGroup struct {
	User     user.UserRouterGroup
	Token    token.JwtTokenRouterGroup
	Chat     chat.ChatServiceRouterGroup
	MService mservice.MServiceRouterGroup
}

var RouterGroupApp = new(RouterGroup)
