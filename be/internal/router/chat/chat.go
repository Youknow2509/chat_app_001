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
	chatRouterPublic.Use(middlewares.TokenAuthMiddleware())
	{
		// TODO: handle get only data from user have access
		chatRouterPublic.GET("get-chat-info", chatController.Chat.GetChatInfo)
		chatRouterPublic.GET("get-list-chat-for-user", chatController.Chat.GetListChatForUser)
		chatRouterPublic.GET("get-user-in-chat", chatController.Chat.GetUserInChat)
	}
	// mprivate router
	chatRouterMPrivate := Router.Group("/chat")
	chatRouterMPrivate.Use(middlewares.AuthenMiddleware())
	chatRouterMPrivate.Use(middlewares.TokenAuthMiddleware())
	{
		chatRouterMPrivate.POST("create-chat-group", chatController.Chat.CreateChatGroup)
		chatRouterMPrivate.POST("create-chat-private", chatController.Chat.CreateChatPrivate)
		chatRouterMPrivate.POST("add-member-to-chat", chatController.Chat.AddMemberToChat)
	}

	// private router
	chatRouterPrivate := Router.Group("/chat") // TODO: handle middleware routes
	chatRouterPrivate.Use(middlewares.AuthenMiddleware())
	chatRouterPrivate.Use(middlewares.TokenAuthMiddleware())
	{
		// TODO: handle add controller
		chatRouterPrivate.POST("upgrade-chat-info")
		chatRouterPrivate.POST("change-admin-group-chat")
		chatRouterPrivate.POST("del-men-from-chat")
		chatRouterPrivate.POST("del-chat")

	}
}
