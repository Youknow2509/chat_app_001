package user

import (
	"strconv"

	"example.com/be/global"
	"example.com/be/internal/model"
	"example.com/be/internal/service"
	"example.com/be/internal/utils/context"
	"example.com/be/response"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

var User = new(cUser)

type cUser struct {
}

// @Summary      Get list user friend
// @Description  Get list user friend 
// @Tags         User Info
// @Accept       json
// @Produce      json
// @Param        Authorization  header  string  true  "Bearer token"
// @Param        limit  query  int  true  "Limit number"
// @Param        page  query  int  true  "Page number"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /v1/user/get_list_friend [get]
func (cU *cUser) GetListUserFriend(c *gin.Context) {
	// get user id from token in headers
	userIDReq, err := context.GetUserIdFromToken(c.Request.Context())
	if err != nil {
		global.Logger.Error("Error getting user id from token", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeUnauthorized, err.Error())
		return
	}
	// query limit and page
	limit := c.Query("limit")
	page := c.Query("page")
	if limit == "" || page == "" {
		response.ErrorResponse(c, response.ErrCodeInvalidInput, "Limit and page are required")
		return
	}
	// convert string to int
	limitInt, err := strconv.Atoi(limit)
	if err != nil {
		global.Logger.Error("Error converting limit to int", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeInvalidInput, err.Error())
		return
	}
	pageInt, err := strconv.Atoi(page)
	if err != nil {
		global.Logger.Error("Error converting page to int", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeInvalidInput, err.Error())
		return
	}
	// validate input
	if limitInt <= 0 {
		global.Logger.Error("Limit must be greater than 0")
		response.ErrorResponse(c, response.ErrCodeInvalidInput, "Limit must be greater than 0")
		return
	}
	if pageInt <= 0 {
		global.Logger.Error("Page must be greater than 0")
		response.ErrorResponse(c, response.ErrCodeInvalidInput, "Page must be greater than 0")
		return
	}
	// create input model	
	input := &model.ListUserFriendInput{
		UserID: userIDReq,
		Limit:  limitInt,
		Page:   pageInt,
	}
	// call to service
	listUserFriend, err := service.UserInfo().ListFriendUser(c.Request.Context(), input)
	if err != nil {
		global.Logger.Error("Error getting list user friend", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeGetListUserFriend, err.Error())
		return
	}
	response.SuccessResponse(c, response.ErrCodeSuccess, listUserFriend)
}

// @Summary      Find user
// @Description  Find a user by mail address
// @Tags         User Info
// @Accept       json
// @Produce      json
// @Param        Authorization  header  string  true  "Bearer token"
// @Param        body body  model.UserFindInput  true  "Find user by email"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /v1/user/find_user [get]
func (cU *cUser) FindUser(c *gin.Context) {
	// get input
	var parameters model.UserFindInput
	if err := c.ShouldBindJSON(&parameters); err != nil {
		global.Logger.Error("Error binding data", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeInvalidInput, err.Error())
		return
	}
	// validate input
	if parameters.UserEmail == "" {
		global.Logger.Error("Email is required")
		response.ErrorResponse(c, response.ErrCodeInvalidInput, "Email is required")
		return
	}
	if parameters.Limit <= 0 {
		global.Logger.Error("Limit must be greater than 0")
		response.ErrorResponse(c, response.ErrCodeInvalidInput, "Limit must be greater than 0")
		return
	}
	if parameters.Page <= 0 {
		global.Logger.Error("Page must be greater than 0")
		response.ErrorResponse(c, response.ErrCodeInvalidInput, "Page must be greater than 0")
		return
	}

	// call to service
	lUserFind, err := service.UserInfo().FindUser(c.Request.Context(), &parameters)
	if err != nil {
		global.Logger.Error("Error finding user", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeFindUser, err.Error())
		return
	}

	response.SuccessResponse(c, response.ErrCodeSuccess, lUserFind)
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
func (cU *cUser) GetUserInfo(c *gin.Context) {
	// get user id from token in headers
	userIDReq, err := context.GetUserIdFromToken(c.Request.Context())
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

// @Summary      Update user info
// @Description  Update nick name, ... information user
// @Tags         User Info
// @Accept       json
// @Produce      json
// @Param        Authorization  header  string  true  "Bearer token"
// @Param        body body  model.UpdateUserInfoInput  true  "Update user info"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /v1/user/update_user_info [put]
func (cU *cUser) UpdateUserInfo(c *gin.Context) {
	var parameters model.UpdateUserInfoInput
	if err := c.ShouldBindJSON(&parameters); err != nil {
		global.Logger.Error("Error binding data", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeInvalidInput, err.Error())
		return
	}
	// get user id from token in headers
	userIDReq, err := context.GetUserIdFromToken(c.Request.Context())
	if err != nil {
		global.Logger.Error("Error getting user id from token", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeUnauthorized, err.Error())
		return
	}
	parameters.UserID = userIDReq
	// call to service
	codeRes, err := service.UserInfo().UpdateUserInfo(c.Request.Context(), &parameters)
	if err != nil {
		global.Logger.Error("Error updating user info", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeUpdateUserInfo, err.Error())
		return
	}
	if codeRes != response.ErrCodeSuccess {
		response.ErrorResponse(c, codeRes, response.GetMessageCode(codeRes))
		return
	}
	response.SuccessResponse(c, response.ErrCodeSuccess, nil)
}

// @Summary      Create friend request
// @Description  Create friend request to another user
// @Tags         User Info
// @Accept       json
// @Produce      json
// @Param        Authorization  header  string  true  "Bearer token"
// @Param        body body  model.CreateFriendRequestInput  true  "Create friend request"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /v1/user/create_friend_request [post]
func (cU *cUser) CreateFriendRequest(c *gin.Context) {
	var parameters model.CreateFriendRequestInput
	if err := c.ShouldBindJSON(&parameters); err != nil {
		global.Logger.Error("Error binding data", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeInvalidInput, err.Error())
		return
	}
	// get user id from token in headers
	userIDReq, err := context.GetUserIdFromToken(c.Request.Context())
	if err != nil {
		global.Logger.Error("Error getting user id from token", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeUnauthorized, err.Error())
		return
	}
	parameters.UserID = userIDReq
	// call to service
	codeRes, err := service.UserInfo().CreateFriendRequest(c.Request.Context(), &parameters)
	if err != nil {
		global.Logger.Error("Error creating friend request", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeCreateFriendRequest, err.Error())
		return
	}
	if codeRes != response.ErrCodeSuccess {
		response.ErrorResponse(c, codeRes, response.GetMessageCode(codeRes))
		return
	}
	response.SuccessResponse(c, response.ErrCodeSuccess, nil)
}

// @Summary      End friend request
// @Description  End friend request to another user
// @Tags         User Info
// @Accept       json
// @Produce      json
// @Param        Authorization  header  string  true  "Bearer token"
// @Param        body body  model.EndFriendRequestInput  true  "End friend request"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /v1/user/end_friend_request [delete]
func (cU *cUser) EndFriendRequest(c *gin.Context) {
	var parameters model.EndFriendRequestInput
	if err := c.ShouldBindJSON(&parameters); err != nil {
		global.Logger.Error("Error binding data", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeInvalidInput, err.Error())
		return
	}
	// get user id from token in headers
	userIDReq, err := context.GetUserIdFromToken(c.Request.Context())
	if err != nil {
		global.Logger.Error("Error getting user id from token", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeUnauthorized, err.Error())
		return
	}
	parameters.UserID = userIDReq
	// call to service
	codeRes, err := service.UserInfo().EndFriendRequest(c.Request.Context(), &parameters)
	if err != nil {
		global.Logger.Error("Error ending friend request", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeEndFriendRequest, err.Error())
		return
	}
	if codeRes != response.ErrCodeSuccess {
		response.ErrorResponse(c, codeRes, response.GetMessageCode(codeRes))
		return
	}
	response.SuccessResponse(c, response.ErrCodeSuccess, nil)
}

// @Summary      Accept friend request
// @Description  Accept friend request from another user
// @Tags         User Info
// @Accept       json
// @Produce      json
// @Param        Authorization  header  string  true  "Bearer token"
// @Param        body body  model.AcceptFriendRequestInput  true  "Accept friend request"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /v1/user/accept_friend_request [post]
func (cU *cUser) AcceptFriendRequest(c *gin.Context) {
	var parameters model.AcceptFriendRequestInput
	if err := c.ShouldBindJSON(&parameters); err != nil {
		global.Logger.Error("Error binding data", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeInvalidInput, err.Error())
		return
	}
	// get user id from token in headers
	userIDReq, err := context.GetUserIdFromToken(c.Request.Context())
	if err != nil {
		global.Logger.Error("Error getting user id from token", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeUnauthorized, err.Error())
		return
	}
	parameters.UserAcceptID = userIDReq
	// call to service
	codeRes, err := service.UserInfo().AcceptFriendRequest(c.Request.Context(), &parameters)
	if err != nil {
		global.Logger.Error("Error accepting friend request", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeEndFriendRequest, err.Error())
		return
	}
	if codeRes != response.ErrCodeSuccess {
		response.ErrorResponse(c, codeRes, response.GetMessageCode(codeRes))
		return
	}
	response.SuccessResponse(c, response.ErrCodeSuccess, nil)
}

// @Summary      Reject friend request
// @Description  Reject friend request from another user
// @Tags         User Info
// @Accept       json
// @Produce      json
// @Param        Authorization  header  string  true  "Bearer token"
// @Param        body body  model.RejectFriendRequestInput  true  "request id"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /v1/user/reject_friend_request [post]
func (cU *cUser) RejectFriendRequest(c *gin.Context) {
	var parameters model.RejectFriendRequestInput
	if err := c.ShouldBindJSON(&parameters); err != nil {
		global.Logger.Error("Error binding data", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeInvalidInput, err.Error())
		return
	}
	// get user id from token in headers
	userIDReq, err := context.GetUserIdFromToken(c.Request.Context())
	if err != nil {
		global.Logger.Error("Error getting user id from token", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeUnauthorized, err.Error())
		return
	}
	parameters.UserAcceptID = userIDReq
	// call to service
	codeRes, err := service.UserInfo().RejectFriendRequest(c.Request.Context(), &parameters)
	if err != nil {
		global.Logger.Error("Error rejecting friend request", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeEndFriendRequest, err.Error())
		return
	}
	if codeRes != response.ErrCodeSuccess {
		response.ErrorResponse(c, codeRes, response.GetMessageCode(codeRes))
		return
	}
	response.SuccessResponse(c, response.ErrCodeSuccess, nil)
}

// @Summary      Delete friend user
// @Description  Delete friend user information from the service
// @Tags         User Info
// @Accept       json
// @Produce      json
// @Param        Authorization  header  string  true  "Bearer token"
// @Param        body body  model.DeleteFriendInput  true  "delete friend user information"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /v1/user/delete_friend [delete]
func (cU *cUser) DeleteFriend(c *gin.Context) {
	var parameters model.DeleteFriendInput
	if err := c.ShouldBindJSON(&parameters); err != nil {
		global.Logger.Error("Error binding data", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeInvalidInput, err.Error())
		return
	}
	// get user id from token in headers
	userIDReq, err := context.GetUserIdFromToken(c.Request.Context())
	if err != nil {
		global.Logger.Error("Error getting user id from token", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeUnauthorized, err.Error())
		return
	}
	parameters.UserID = userIDReq
	// call to service
	codeRes, err := service.UserInfo().DeleteFriend(c.Request.Context(), &parameters)
	if err != nil {
		global.Logger.Error("Error deleting friend", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeDeleteFriend, err.Error())
		return
	}
	if codeRes != response.ErrCodeSuccess {
		response.ErrorResponse(c, codeRes, response.GetMessageCode(codeRes))
		return
	}
	response.SuccessResponse(c, response.ErrCodeSuccess, nil)
}

// @Summary      Get list friend request
// @Description  Get list friend request of user
// @Tags         User Info
// @Accept       json
// @Produce      json
// @Param        Authorization  header  string  true  "Bearer token"
// @Param        limit  query  int  true  "Limit number"
// @Param        page  query  int  true  "Page number"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /v1/user/get_list_friend_request [get]
func (cU *cUser) GetListFriendRequet(c *gin.Context) {
	// query limit and page
	limit := c.Query("limit")
	page := c.Query("page")
	if limit == "" || page == "" {
		response.ErrorResponse(c, response.ErrCodeInvalidInput, "Limit and page are required")
		return
	}
	// convert string to int
	limitInt, err := strconv.Atoi(limit)
	if err != nil {
		global.Logger.Error("Error converting limit to int", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeInvalidInput, err.Error())
		return
	}
	pageInt, err := strconv.Atoi(page)
	if err != nil {
		global.Logger.Error("Error converting page to int", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeInvalidInput, err.Error())
		return
	}
	// get user id from token in headers
	userIDReq, err := context.GetUserIdFromToken(c.Request.Context())
	if err != nil {
		global.Logger.Error("Error getting user id from token", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeUnauthorized, err.Error())
		return
	}
	// create input model
	input := &model.GetFriendRequestInput{
		UserID: userIDReq,
		Limit:  limitInt,
		Page:   pageInt,
	}
	// call to service
	listFriendRequest, err := service.UserInfo().GetListFriendRequest(c.Request.Context(), input)
	if err != nil {
		global.Logger.Error("Error getting list friend request", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeGetListFriendRequest, err.Error())
		return
	}

	response.SuccessResponse(c, response.ErrCodeSuccess, listFriendRequest)
}

// @Summary      Update password user for user
// @Description  Update password user for user
// @Tags         User Info
// @Accept       json
// @Produce      json
// @Param        Authorization  header  string  true  "Bearer token"
// @Param        body body  model.UserChangePasswordInput  true  "Update password user for user"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /v1/user/update_password [put]
func (cU *cUser) UpdatePassword(c *gin.Context) {
	var parameters model.UserChangePasswordInput
	if err := c.ShouldBindJSON(&parameters); err != nil {
		global.Logger.Error("Error binding data", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeInvalidInput, err.Error())
		return
	}
	// get id user from token
	userIDReq, err := context.GetUserIdFromToken(c.Request.Context())
	if err != nil {
		global.Logger.Error("Error getting user id from token", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeUnauthorized, err.Error())
		return
	}
	parameters.UserID = userIDReq
	// call to service
	codeRes, err := service.UserInfo().UpdatePasswordForUserRequest(c.Request.Context(), &parameters)
	if err != nil {
		global.Logger.Error("Error updating password", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeUpdatePassword, err.Error())
		return
	}
	if codeRes != response.ErrCodeSuccess {
		response.ErrorResponse(c, codeRes, response.GetMessageCode(codeRes))
		return
	}

	response.SuccessResponse(c, response.ErrCodeSuccess, nil)
}
