package user

import (
	"example.com/be/internal/controller/account"
	"example.com/be/internal/middlewares"
	"github.com/gin-gonic/gin"
	userInfoController "example.com/be/internal/controller/user"
)

type UserRouter struct {
}

func (ur *UserRouter) InitUserRouter(Router *gin.RouterGroup) {
	// public router
	userRouterPublic := Router.Group("/user")
	{
		userRouterPublic.POST("/register", account.Login.Register)
		userRouterPublic.POST("/login", account.Login.Login)
		userRouterPublic.POST("/verify_account", account.Login.VerifyOTP)
		userRouterPublic.POST("/upgrade_password_register", account.Login.UpgradePasswordRegister)
		userRouterPublic.POST("logout") // TODO
	}

	// private router
	userRouterPrivate := Router.Group("/user")
	userRouterPrivate.Use(middlewares.AuthenMiddleware())
	userRouterPrivate.Use(middlewares.TokenAuthMiddleware())
	{
		// TODO
		userRouterPrivate.GET("/get_user_info", userInfoController.User.GetUserInfo)
		userRouterPrivate.GET("/find_user", userInfoController.User.FindUser)
		userRouterPrivate.POST("/update_user_info", userInfoController.User.UpdateUserInfo)
		userRouterPrivate.POST("/create_friend_request", userInfoController.User.CreateFriendRequest)
		userRouterPrivate.POST("/end_friend_request")
		userRouterPrivate.POST("/delete_friend")
		userRouterPrivate.POST("/accept_friend_request")
		userRouterPrivate.POST("/reject_friend_request")
		userRouterPrivate.GET("/get_list_friend_request")
		//
		userRouterPrivate.POST("/update_password")
	}
}
