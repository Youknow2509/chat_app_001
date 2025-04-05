package mservice

import (
	"strconv"

	"example.com/be/global"
	"example.com/be/internal/model"
	"example.com/be/internal/service"
	"example.com/be/response"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

var User = new(cUser)

type cUser struct {
}

// @Summary      Show detail friend request
// @Description  Get detail friend request with user id
// @Tags         Microservice
// @Accept       json
// @Produce      json
// @Param        Authorization header string true "Authorization Bearer token microservice (Eg: Bearer 123456)"
// @Param        user1 query string true "user 1"
// @Param        user2 query string true "user 2"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /api/v1/mservice/get-detail-friend-req [get]
func (ct *cUser) GetDetailFriendRequest(c *gin.Context) {
	// query user id
	userID1 := c.Query("user1")
	if userID1 == "" {
		response.ErrorResponse(c, response.ErrCodeBindTokenInput, "User ID 1 is required")
		return
	}
	userID2 := c.Query("user2")
	if userID2 == "" {
		response.ErrorResponse(c, response.ErrCodeBindTokenInput, "User ID 2 is required")
		return
	}
	// model input to service
	p := &model.CheckFriendUserInput{
		User1: userID1,
		User2: userID2,
	}
	// call to service
	code, res, err := service.UserInfo().CheckFriendUser(c, p)
	if code != response.ErrCodeSuccess {
        global.Logger.Error("Error checking friend user", zap.Error(err))
        response.ErrorResponse(c, code, response.GetMessageCode(code))
        return
    }
	if err != nil {
		global.Logger.Error("Error checking friend user", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeCheckFriendRequest, err.Error())
		return
	}

	response.SuccessResponse(c, response.ErrCodeSuccess, res)
}


// @Summary      Hanlde get list chat of user
// @Description  Get list chat of user with user id
// @Tags         Microservice
// @Accept       json
// @Produce      json
// @Param        Authorization header string true "Authorization Bearer token microservice (Eg: Bearer 123456)"
// @Param        user_id query string true "user ID"
// @Param        limit query string true "limit number of chat"
// @Param        page query string true "page number"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /api/v1/mservice/get-chats-user [get]
func (ct *cUser) GetChatsUser(c *gin.Context) {
	// query user id
    userID := c.Query("user_id")
    if userID == "" {
        response.ErrorResponse(c, response.ErrCodeBindTokenInput, "User ID is required")
        return
    }
    // query limit and page
    limmit := c.Query("limit")
    if limmit == "" {
        response.ErrorResponse(c, response.ErrCodeBindTokenInput, "Limit is required")
        return
    }
    page := c.Query("page")
    if page == "" {
        response.ErrorResponse(c, response.ErrCodeBindTokenInput, "Page is required")
        return
    }
    // convert limmit and page to int
    limmitInt, err := strconv.Atoi(limmit)
	if err != nil {
		response.ErrorResponse(c, response.ErrCodeInvalidInput, "Invalid limmit value")
		return
	}
	pageInt, err := strconv.Atoi(page)
	if err != nil {
		response.ErrorResponse(c, response.ErrCodeInvalidInput, "Invalid page value")
		return
	}
	if limmitInt < 0 || pageInt < 0 {
		response.ErrorResponse(c, response.ErrCodeInvalidInput, "Limmit and page must be greater than 0")
		return
	}
	// model input to service
	// create model input
	p := &model.InputGetChatForUser{
		UserID: userID,
		Limit:  limmitInt,
		Page:   pageInt,
	}
	// call to service
	outputData, code, err := service.ChatService().GetListChatForUser(c, p)
	if code != response.ErrCodeSuccess {
		global.Logger.Error("Error getting list chat", zap.Error(err))
		response.ErrorResponse(c, code, response.GetMessageCode(code))
		return
	}
	if err != nil {
		global.Logger.Error("Error getting list chat", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeGetListChat, err.Error())
		return
	}
	response.SuccessResponse(c, response.ErrCodeSuccess, outputData)
}