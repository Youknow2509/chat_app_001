package chat

import (
	"example.com/be/internal/model"
	"example.com/be/internal/service"
	"example.com/be/internal/utils/context"
	"example.com/be/response"
	"github.com/gin-gonic/gin"
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
	
	chatID := c.Query("chat_id")
    if chatID == "" {
        response.ErrorResponse(c, response.ErrCodeBindTokenInput, "Chat ID is required")
        return
    }

    p := model.InputGetChatInfor{
        ChatID: chatID,
    }

	outputData, err := service.ChatService().GetChatInfo(c, &p)
	if err != nil {
		response.ErrorResponse(c, response.ErrCodeGetChatInfo, err.Error())
		return
	}

	response.SuccessResponse(c, response.ErrCodeSuccess, outputData)
}
