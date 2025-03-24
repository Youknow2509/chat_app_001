package token

import (
	"fmt"

	"example.com/be/global"
	"example.com/be/internal/model"
	"example.com/be/internal/utils/auth"
	"example.com/be/response"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

var Jwt = new(cJwtToken)

type cJwtToken struct {

}

// TODO: handle test - delete when deloy production
// Create a new JWT token 
// @Summary      Create a new JWT token test
// @Description  Testing create a new JWT token
// @Tags         Token
// @Accept       json
// @Produce      json
// @Param        payload body model.JwtInput true "payload"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /v1/token/create_token [post]
func (cJ *cJwtToken) CreateToken(c *gin.Context) {

	var params model.JwtInput
	if err := c.ShouldBindJSON(&params); err != nil {
		response.ErrorResponse(c, response.ErrCodeBindTokenInput, err.Error())
		return
	}

	// create a new token
	out, err := auth.CreateToken(params.Data)
	if err != nil {
        global.Logger.Error("Error creating token", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeCreateToken, err.Error())
		return
	}
	fmt.Println("Access token created: ", out)

	response.SuccessResponse(c, response.ErrCodeSuccess, out)
}

// TODO: handle test - delete when deloy production
// Create a new JWT refresh token 
// @Summary      Create a new JWT token test
// @Description  Testing create a new refresh JWT token
// @Tags         Token
// @Accept       json
// @Produce      json
// @Param        payload body model.JwtInput true "payload"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /v1/token/create_refresh_token [post]
func (cJ *cJwtToken) CreateRefreshToken(c *gin.Context) {

	var params model.JwtInput
	if err := c.ShouldBindJSON(&params); err != nil {
		response.ErrorResponse(c, response.ErrCodeBindTokenInput, err.Error())
		return
	}

	// create a new token
	out, err := auth.CreateRefreshToken(params.Data)
	if err != nil {
        global.Logger.Error("Error creating refresh token", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeCreateToken, err.Error())
		return
	}
	fmt.Println("Refresh token created: ", out)

	response.SuccessResponse(c, response.ErrCodeSuccess, out)
}

// Jwt validate token
// @Summary      Validate token for access token and refresh token
// @Description  Validate token for access token and refresh token
// @Tags         Token
// @Accept       json
// @Produce      json
// @Param        payload body model.JwtInput true "payload"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /v1/token/valid_token [post]
func (cJ *cJwtToken) JwtValidToken(c *gin.Context) {

	var params model.JwtInput
	if err := c.ShouldBindJSON(&params); err != nil {
		response.ErrorResponse(c, response.ErrCodeBindTokenInput, err.Error())
		return
	}

	// create a new token
	out, err := auth.ValidateTokenSubject(params.Data)
	if err != nil {
        global.Logger.Error("Error creating validate token", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeTokenInvalid, err.Error())
		return
	}
	fmt.Printf("Token: %s --> validate: true\n", params.Data)

	response.SuccessResponse(c, response.ErrCodeSuccess, out)
}

// Handle refresh token request
func (cJ *cJwtToken) RefreshToken(c *gin.Context) {

	var params model.JwtRefreshInput
	if err := c.ShouldBindJSON(&params); err != nil {
		response.ErrorResponse(c, response.ErrCodeBindTokenInput, err.Error())
		return
	}

	// TODO: todo cmp
	response.SuccessResponse(c, response.ErrCodeSuccess, "TODO complete")
}
