package chat

import (
	"fmt"
	"strconv"

	"example.com/be/global"
	"example.com/be/internal/model"
	"example.com/be/internal/service"
	"example.com/be/internal/utils/context"
	"example.com/be/response"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

var Chat = new(cChat)

type cChat struct {
}

// Create a chat group
// @Summary      Create a chat group with user after auth
// @Description  Create a chat group with user after auth, user create is admin and others users are members
// @Tags         Chat
// @Accept       json
// @Produce      json
// @Param        Authorization header string true "Authorization Bearer token"
// @Param        payload body model.CreateChatGroupInput true "payload"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /v1/chat/create-chat-group [post]
func (ct *cChat) CreateChatGroup(c *gin.Context) {
	var inputChatGroup model.CreateChatGroupInput
	if err := c.ShouldBindJSON(&inputChatGroup); err != nil {
		response.ErrorResponse(c, response.ErrCodeBindTokenInput, err.Error())
		return
	}
	// get user id from token
	userID, err := context.GetUserIdFromUUID(c.Request.Context())
	if err != nil {
		response.ErrorResponse(c, response.ErrCodeUnauthorized, err.Error())
		return
	}
	inputChatGroup.UserIDCreate = userID

	codeRes, outputData, err := service.ChatService().CreateChatGroup(c, &inputChatGroup)
	if err != nil {
		response.ErrorResponse(c, codeRes, err.Error())
		return
	}

	response.SuccessResponse(c, response.ErrCodeSuccess, outputData)
}

// Create a chat private
// @Summary      Create a chat private
// @Description  Create a chat private both user
// @Tags         Chat
// @Accept       json
// @Produce      json
// @Param        Authorization header string true "Authorization Bearer token"
// @Param        payload body model.CreateChatPrivateInput true "payload"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /v1/chat/create-chat-private [post]
func (ct *cChat) CreateChatPrivate(c *gin.Context) {
	var inputChatGroup model.CreateChatPrivateInput
	if err := c.ShouldBindJSON(&inputChatGroup); err != nil {
		response.ErrorResponse(c, response.ErrCodeBindTokenInput, err.Error())
		return
	}
	// get user id from token
	userID, err := context.GetUserIdFromUUID(c.Request.Context())
	if err != nil {
		response.ErrorResponse(c, response.ErrCodeUnauthorized, err.Error())
		return
	}
	inputChatGroup.User1 = userID

	codeRes, outputData, err := service.ChatService().CreateChatPrivate(c, &inputChatGroup)
	if err != nil {
		response.ErrorResponse(c, codeRes, err.Error())
		return
	}

	response.SuccessResponse(c, response.ErrCodeSuccess, outputData)
}

// Add member to chat
// @Summary      Add member to chat
// @Description  Add member to chat by admin group member
// @Tags         Chat
// @Accept       json
// @Produce      json
// @Param        Authorization header string true "Authorization Bearer token"
// @Param        payload body model.AddMemberToChatInput true "payload"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /v1/chat/add-member-to-chat [post]
func (ct *cChat) AddMemberToChat(c *gin.Context) {
	var parameters *model.AddMemberToChatInput
	if err := c.ShouldBindJSON(&parameters); err != nil {
		response.ErrorResponse(c, response.ErrCodeBindTokenInput, err.Error())
		return
	}
	// get user id from token
	userIDReq, err := context.GetUserIdFromUUID(c.Request.Context())
	if err != nil {
		response.ErrorResponse(c, response.ErrCodeUnauthorized, err.Error())
		return
	}
	parameters.AdminChatID = userIDReq
	// call to service
	codeResult, out, err := service.ChatService().AddMemberToChat(c, parameters)
	if err != nil {
		global.Logger.Error("Error adding member to chat", zap.Error(err))
		response.ErrorResponse(c, codeResult, err.Error())
		return
	}
	if out.TypeAdd == "group" {
		global.Logger.Info(fmt.Sprintf("Created new group chat with id: %s", out.ChatID))
		response.SuccessResponse(c, response.ErrCodeCreateChatGroupSuccess, out)
		return
	}

	response.SuccessResponse(c, response.ErrCodeSuccess, nil)
}

// Get chat info
// @Summary      Get chat information
// @Description  Get chat information by chat id
// @Tags         Chat
// @Accept       json
// @Produce      json
// @Param        Authorization header string true "Authorization Bearer token"
// @Param        chat_id query string true "Chat ID"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /v1/chat/get-chat-info [get]
func (ct *cChat) GetChatInfo(c *gin.Context) {
	// query chat id
	chatID := c.Query("chat_id")
	if chatID == "" {
		response.ErrorResponse(c, response.ErrCodeBindTokenInput, "Chat ID is required")
		return
	}
	// get user id from token
	userIDReq, err := context.GetUserIdFromUUID(c.Request.Context())
	if err != nil {
		response.ErrorResponse(c, response.ErrCodeUnauthorized, err.Error())
		return
	}
	// create model input
	p := model.InputGetChatInfor{
		ChatID: chatID,
		UserID: userIDReq,
	}
	// call to service
	outputData, err := service.ChatService().GetChatInfo(c, &p)
	if err != nil {
		response.ErrorResponse(c, response.ErrCodeGetChatInfo, err.Error())
		return
	}

	response.SuccessResponse(c, response.ErrCodeSuccess, outputData)
}

// @Summary      Get list of chat
// @Description  Get list chat from user
// @Tags         Chat
// @Accept       json
// @Produce      json
// @Param        Authorization header string true "Authorization Bearer token"
// @Param        limit query string true "limit number of chat"
// @Param        page query string true "page number"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /v1/chat/get-list-chat-for-user [get]
func (ct *cChat) GetListChatForUser(c *gin.Context) {
	// query limit and page
	limit := c.Query("limit")
	page := c.Query("page")
	if limit == "" || page == "" {
		response.ErrorResponse(c, response.ErrCodeBindTokenInput, "Limit and page are required")
		return
	}
	// get user id from token
	userIDReq, err := context.GetUserIdFromUUID(c.Request.Context())
	if err != nil {
		response.ErrorResponse(c, response.ErrCodeUnauthorized, err.Error())
		return
	}
	if userIDReq == "" {
		response.ErrorResponse(c, response.ErrCodeUnauthorized, "User ID is required")
		return
	}
	// convert limit and page from string to int
	limitInt, err := strconv.Atoi(limit)
	if err != nil {
		response.ErrorResponse(c, response.ErrCodeInvalidInput, "Invalid limit value")
		return
	}
	pageInt, err := strconv.Atoi(page)
	if err != nil {
		response.ErrorResponse(c, response.ErrCodeInvalidInput, "Invalid page value")
		return
	}
	// create model input
	p := &model.InputGetChatForUser{
		UserID: userIDReq,
		Limit:  limitInt,
		Page:   pageInt,
	}
	// call to service
	outputData, _, err := service.ChatService().GetListChatForUser(c, p)
	if err != nil {
		global.Logger.Error("Error getting list chat", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeGetListChat, err.Error())
		return
	}
	response.SuccessResponse(c, response.ErrCodeSuccess, outputData)
}


// @Summary      Hanlde get infomation user in chat 
// @Description  Get information user in chat with chat id 
// @Tags         Chat
// @Accept       json
// @Produce      json
// @Param        Authorization header string true "Authorization Bearer token"
// @Param        chat_id query string true "Chat ID"
// @Param        limit query string true "limit number of chat"
// @Param        page query string true "page number"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /v1/chat/get-user-in-chat [get]
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
	// get user id from token
	userIDReq, err := context.GetUserIdFromUUID(c.Request.Context())
	if err != nil {
		global.Logger.Error("Error getting user id from token", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeUnauthorized, err.Error())
		return
	}
	// model input to service
	p := &model.InputGetUserInChat{
		UserId: userIDReq,
		ChatID: chatID,
		Limit:  limmitInt,
        Page:   pageInt,
    }
	// call to service 
	outPutData, err := service.ChatService().GetUserInChat(c, p)
	if err != nil {
		global.Logger.Error("Error getting user in chat", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeGetUserInChat, err.Error())
		return
	}

	response.SuccessResponse(c, response.ErrCodeSuccess, outPutData)
}

// @Summary      Change admin chat status
// @Description  Change admin chat status from old admin to new admin
// @Tags         Chat
// @Accept       json
// @Produce      json
// @Param        Authorization header string true "Authorization Bearer token"
// @Param        payload body model.ChangeAdminGroupChatInput true "payload"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /v1/chat/change-admin-group-chat [post]
func (ct *cChat) ChangeAdminGroupChat(c *gin.Context) {
	// get body input
	var parameters *model.ChangeAdminGroupChatInput
	if err := c.ShouldBindJSON(&parameters); err != nil {
		global.Logger.Error("Error binding input", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeBindTokenInput, err.Error())
		return
	}
	// get user id from token
	userIDReq, err := context.GetUserIdFromUUID(c.Request.Context())
	if err != nil {
		global.Logger.Error("Error getting user id from token", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeUnauthorized, err.Error())
		return
	}
	parameters.OldAdminID = userIDReq
	// call to service
	codeRes, err := service.ChatServiceAdmin().ChangeAdminGroupChat(c, parameters)
	if err != nil {
		global.Logger.Error("Error changing admin group chat", zap.Error(err))
		response.ErrorResponse(c, codeRes, err.Error())
		return
	}
	if codeRes != response.ErrCodeSuccess {
		response.ErrorResponse(c, codeRes, response.GetMessageCode(codeRes))
		return
	}
	response.SuccessResponse(c, response.ErrCodeSuccess, nil)
}

// @Summary      Delete chat
// @Description  Delete chat by admin group chat
// @Tags         Chat
// @Accept       json
// @Produce      json
// @Param        Authorization header string true "Authorization Bearer token"
// @Param        payload body model.DelChatInput true "payload"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /v1/chat/del-chat [post]
func (ct *cChat) DelChat(c *gin.Context) {
	// get body input
	var parameters *model.DelChatInput
	if err := c.ShouldBindJSON(&parameters); err != nil {
		global.Logger.Error("Error binding input", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeBindTokenInput, err.Error())
		return
	}
	// get user id from token
	userIDReq, err := context.GetUserIdFromUUID(c.Request.Context())
	if err != nil {
		global.Logger.Error("Error getting user id from token", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeUnauthorized, err.Error())
		return
	}
	parameters.AdminChatID = userIDReq
	// call to service
	codeRes, err := service.ChatServiceAdmin().DelChat(c, parameters)
	if err != nil {
        global.Logger.Error("Error deleting chat", zap.Error(err))
        response.ErrorResponse(c, codeRes, err.Error())
        return
    }
	if codeRes != response.ErrCodeSuccess {
		response.ErrorResponse(c, codeRes, response.GetMessageCode(codeRes))
		return
	}

	response.SuccessResponse(c, response.ErrCodeSuccess, nil)
}

// @Summary      Delete member from chat
// @Description  Delete member from chat by admin group chat
// @Tags         Chat
// @Accept       json
// @Produce      json
// @Param        Authorization header string true "Authorization Bearer token"
// @Param        payload body model.DelMenForChatInput true "payload"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /v1/chat/del-men-from-chat [post]
func (ct *cChat) DelMemberForChat(c *gin.Context) {
	// get body input
	var parameters *model.DelMenForChatInput
	if err := c.ShouldBindJSON(&parameters); err != nil {
		global.Logger.Error("Error binding input", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeBindTokenInput, err.Error())
		return
	}
	// get user id from token
	userIDReq, err := context.GetUserIdFromUUID(c.Request.Context())
	if err != nil {
		global.Logger.Error("Error getting user id from token", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeUnauthorized, err.Error())
		return
	}
	parameters.AdminChatID = userIDReq
	// call to service
	codeRes, err := service.ChatServiceAdmin().DelMenForChat(c, parameters)
	if err != nil {
		global.Logger.Error("Error deleting member from chat", zap.Error(err))
		response.ErrorResponse(c, codeRes, err.Error())
		return
	}
	if codeRes != response.ErrCodeSuccess {
		response.ErrorResponse(c, codeRes, response.GetMessageCode(codeRes))
		return
	}

	response.SuccessResponse(c, response.ErrCodeSuccess, nil)
}