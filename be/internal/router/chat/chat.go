package chat

import (
	chatController "example.com/be/internal/controller/chat"
	"example.com/be/internal/middlewares"
	"github.com/gin-gonic/gin"
)

// chat base router
type ChatBaseRouter struct {

}

func (cb *ChatBaseRouter) InitChatBaseRouter(Router *gin.RouterGroup) {
	// public router
	chatRouterPublic := Router.Group("/chat")
	chatRouterPublic.Use(middlewares.AuthenMiddleware())
	{
		chatRouterPublic.POST("create-chat-group", chatController.Chat.CreateChatGroup)
		chatRouterPublic.POST("create-chat-private", chatController.Chat.CreateChatPrivate)
		// TODO: handle add controller
		chatRouterPublic.GET("get-chat-info", chatController.Chat.GetChatInfo)
		chatRouterPublic.GET("get-list-chat-for-user")
		chatRouterPublic.GET("get-user-in-chat")
		chatRouterPublic.POST("add-member-to-chat")
	}

	// private router
	chatRouterPrivate := Router.Group("/chat") // TODO: handle middleware routes
	chatRouterPublic.Use(middlewares.AuthenMiddleware())
	{
		// TODO: handle add controller
		chatRouterPrivate.POST("upgrade-chat-info")
		chatRouterPrivate.POST("change-admin-group-chat")
		chatRouterPrivate.POST("del-men-from-chat")
		chatRouterPrivate.POST("del-chat")
		
	}
}