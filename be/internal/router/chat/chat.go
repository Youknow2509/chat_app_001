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
	// TODO: handle middleware limit request
	// public router
	chatRouterPublic := Router.Group("/chat")
	chatRouterPublic.Use(middlewares.AuthenMiddleware())
	chatRouterPublic.Use(middlewares.TokenAuthMiddleware())
	{
		chatRouterPublic.GET("get-chat-info", chatController.Chat.GetChatInfo)
		chatRouterPublic.GET("get-list-chat-for-user", chatController.Chat.GetListChatForUser)
		chatRouterPublic.GET("get-user-in-chat", chatController.Chat.GetUserInChat)
		chatRouterPublic.GET("get-list-chat-private-for-user", chatController.Chat.GetListChatPrivateForUser)
		chatRouterPublic.GET("get-list-chat-group-for-user", chatController.Chat.GetListChatGroupForUser)
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
	chatRouterPrivate := Router.Group("/chat") 
	chatRouterPrivate.Use(middlewares.AuthenMiddleware())
	chatRouterPrivate.Use(middlewares.TokenAuthMiddleware())
	{
		chatRouterPrivate.PUT("upgrade-chat-info", chatController.Chat.UpdateChatInfo)
		chatRouterPrivate.PUT("change-admin-group-chat", chatController.Chat.ChangeAdminGroupChat)
		chatRouterPrivate.DELETE("del-men-from-chat", chatController.Chat.DelMemberForChat)
		chatRouterPrivate.DELETE("del-chat", chatController.Chat.DelChat)
	}
}
