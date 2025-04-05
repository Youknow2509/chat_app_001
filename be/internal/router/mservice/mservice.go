package mservice

import (
	"example.com/be/internal/controller/mservice"
	"example.com/be/internal/middlewares"
	"github.com/gin-gonic/gin"
)

// ChatServiceRouter struct
type ChatServiceRouter struct {

}

func (r *ChatServiceRouter) InitChatServiceRouter(Router *gin.RouterGroup) {
	// base router
	baseRouter := Router.Group("mservice")
	baseRouter.Use(middlewares.AuthenMServiceMiddleware())
	{
		baseRouter.GET("get-user-in-chat", mservice.Chat.GetUserInChat)
		baseRouter.GET("get-chats-user", mservice.Chat.GetChatsUser)
		baseRouter.GET("get-chats-private-user", mservice.Chat.GetChatsPrivateUser)
		baseRouter.GET("get-chats-group-user", mservice.Chat.GetChatsGroupUser)

	}
}