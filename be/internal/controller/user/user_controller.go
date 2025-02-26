package user

import (
	"example.com/be/global"
	"example.com/be/internal/service"
	"example.com/be/internal/utils/context"
	"example.com/be/response"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

var User = new(cUser)

type cUser struct {
}

// @Summary      Get user info 
// @Description  Get user information after login
// @Tags         User Info
// @Accept       json
// @Produce      json
// @Param        Authorization  header  string  true  "Bearer token"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /v1/user/get_user_info [get]
func (cU *cUser) GetUserInfoController(c *gin.Context) {
    // get user id from token in headers
	userIDReq, err := context.GetUserIdFromUUID(c.Request.Context())
	if err != nil {
		global.Logger.Error("Error getting user id from token", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeUnauthorized, err.Error())
		return
	}
	// call to service
	userInfo, err := service.UserInfo().GetUserInfo(c.Request.Context(), userIDReq)
	if err != nil {
		global.Logger.Error("Error getting user info", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeGetUserInfo, err.Error())
		return
	}
	response.SuccessResponse(c, response.ErrCodeSuccess, userInfo)
}