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
		userRouterPublic.POST("/logout", account.Login.Logout)
		userRouterPublic.POST("/refresh_token", account.Login.RefreshToken) 
		userRouterPublic.POST("/forgot_password", account.Login.ForgotPassword)
		userRouterPublic.GET("/verify_forgot_password/:email/:token", account.Login.VerifyForgotPassword)
	}

	// private router
	userRouterPrivate := Router.Group("/user")
	userRouterPrivate.Use(middlewares.AuthenMiddleware())
	userRouterPrivate.Use(middlewares.TokenAuthMiddleware())
	{
		userRouterPrivate.GET("/get_user_info", userInfoController.User.GetUserInfo)
		userRouterPrivate.GET("/find_user", userInfoController.User.FindUser)
		userRouterPrivate.GET("/get_list_friend", userInfoController.User.GetListUserFriend)
		userRouterPrivate.PUT("/update_user_info", userInfoController.User.UpdateUserInfo)
		userRouterPrivate.POST("/create_friend_request", userInfoController.User.CreateFriendRequest)
		userRouterPrivate.DELETE("/end_friend_request", userInfoController.User.EndFriendRequest)
		userRouterPrivate.DELETE("/delete_friend", userInfoController.User.DeleteFriend)
		userRouterPrivate.POST("/accept_friend_request", userInfoController.User.AcceptFriendRequest)
		userRouterPrivate.POST("/reject_friend_request", userInfoController.User.RejectFriendRequest)
		userRouterPrivate.GET("/get_list_friend_request", userInfoController.User.GetListFriendRequet)
		//
		userRouterPrivate.PUT("/update_password", userInfoController.User.UpdatePassword)
	}
}
