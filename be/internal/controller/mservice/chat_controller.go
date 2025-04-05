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

var Chat = new(cChat)

type cChat struct {
}

// @Summary      Hanlde get infomation user in chat
// @Description  Get information user in chat with chat id
// @Tags         Microservice
// @Accept       json
// @Produce      json
// @Param        Authorization header string true "Authorization Bearer token microservice (Eg: Bearer 123456)"
// @Param        chat_id query string true "Chat ID"
// @Param        limit query string true "limit number of chat"
// @Param        page query string true "page number"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /api/v1/mservice/get-user-in-chat [get]
func (ct *cChat) GetUserInChat(c *gin.Context) {
	// query chat id
	chatID := c.Query("chat_id")
	if chatID == "" {
		response.ErrorResponse(c, response.ErrCodeBindTokenInput, "Chat ID is required")
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
	p := &model.InputGetUserInChatAdmin{
		ChatID: chatID,
		Limit:  limmitInt,
		Page:   pageInt,
	}
	// call to service
	outPutData, err := service.ChatServiceAdmin().GetUserInChatAdmin(c, p)
	if err != nil {
		global.Logger.Error("Error getting user in chat", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeGetUserInChat, err.Error())
		return
	}

	response.SuccessResponse(c, response.ErrCodeSuccess, outPutData)
}

// @Summary      Hanlde get list chat private of user
// @Description  Get list chat private of user with user id
// @Tags         Microservice
// @Accept       json
// @Produce      json
// @Param        Authorization header string true "Authorization Bearer token microservice (Eg: Bearer 123456)"
// @Param        user_id query string true "user ID"
// @Param        limit query string true "limit number of chat"
// @Param        page query string true "page number"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /api/v1/mservice/get-chats-private-user [get]
func (ct *cChat) GetChatsPrivateUser(c *gin.Context) {
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
	outputData, code, err := service.ChatService().UserGetListChatPrivateForUser(c, p)
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

// @Summary      Hanlde get list chat group of user
// @Description  Get list chat group of user with user id
// @Tags         Microservice
// @Accept       json
// @Produce      json
// @Param        Authorization header string true "Authorization Bearer token microservice (Eg: Bearer 123456)"
// @Param        user_id query string true "user ID"
// @Param        limit query string true "limit number of chat"
// @Param        page query string true "page number"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /api/v1/mservice/get-chats-group-user [get]
func (ct *cChat) GetChatsGroupUser(c *gin.Context) {
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
	outputData, code, err := service.ChatService().UserGetListChatGroupForUser(c, p)
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