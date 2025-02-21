package token

import (
	"github.com/gin-gonic/gin"
	tokenController "example.com/be/internal/controller/token"
)

type JwtTokenRouter struct {
	
}

// init jwt token router 
func (jtk *JwtTokenRouter) InitTokenRouter(Router *gin.RouterGroup) {
	// public router
	jwtRouterPublic := Router.Group("/token")
	{
		// TODO: handle test - delete when deloy production
		jwtRouterPublic.POST("/create_token", tokenController.Jwt.CreateToken)
		jwtRouterPublic.POST("/create_refresh_token", tokenController.Jwt.CreateRefreshToken)
		// token validate
		jwtRouterPublic.POST("/valid_token", tokenController.Jwt.JwtValidToken)
		// token refresh
		jwtRouterPublic.POST("/refreshToken")
	}

	// private router
}
