package account

import (
	"regexp"

	"example.com/be/global"
	"example.com/be/internal/model"
	"example.com/be/internal/service"
	"example.com/be/response"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

var Login = new(cUserLogin)

type cUserLogin struct {
}


// @Summary      Update name and avatar when register
// @Description  Update name and avatar when register 
// @Tags         accounts management
// @Accept       json
// @Produce      json
// @Param        body body model.UpdateNameAndAvatarRegisterInput true "payload"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /api/v1/user/update_user_name_and_avatar [post]
func (u *cUserLogin) UpdateUserNameAndAvatar(c *gin.Context) {
	var params model.UpdateNameAndAvatarRegisterInput
	if err := c.ShouldBindJSON(&params); err != nil {
		response.ErrorResponse(c, response.ErrCodeBindVerifyInput, err.Error())
		return
	}
	codeStatus, err := service.UserLogin().UpdateNameAndAvatarRegister(c, &params)
	if codeStatus != response.ErrCodeSuccess {
		global.Logger.Error("Error update user name and avatar", zap.Error(err))
        response.ErrorResponse(c, codeStatus, response.GetMessageCode(codeStatus))
        return
	}
	if err != nil {
		global.Logger.Error("Error update user name and avatar", zap.Error(err))
		response.ErrorResponse(c, codeStatus, err.Error())
		return
	}
	response.SuccessResponse(c, response.ErrCodeSuccess, nil)
}

// Login godoc
// @Summary      Login user
// @Description  Login user by account and password
// @Tags         accounts management
// @Accept       json
// @Produce      json
// @Param        payload body model.LoginInput true "payload"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /api/v1/user/login [post]
func (cU *cUserLogin) Login(c *gin.Context) {

	var params model.LoginInput
	if err := c.ShouldBindJSON(&params); err != nil {
		response.ErrorResponse(c, response.ErrCodeBindLoginInput, err.Error())
		return
	}

	codeStatus, out, err := service.UserLogin().Login(c, &params)
	if codeStatus == response.ErrCodeUserNotFound {
		response.SuccessResponse(c, codeStatus, response.GetMessageCode(codeStatus))
		return
	}
	if codeStatus == response.ErrCodeAuthFailed {
		response.SuccessResponse(c, codeStatus, response.GetMessageCode(codeStatus))
		return
	}
	if err != nil {
		global.Logger.Error("Error login user", zap.Error(err))
		response.ErrorResponse(c, codeStatus, err.Error())
		return
	}

	response.SuccessResponse(c, response.ErrCodeSuccess, out)
}

// Register godoc
// @Summary      Register a new account
// @Description  When user register, system will send OTP to user's phone number or email address
// @Tags         accounts management
// @Accept       json
// @Produce      json
// @Param        payload body model.RegisterInput true "payload"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /api/v1/user/register [post]
func (cU *cUserLogin) Register(c *gin.Context) {
	var params model.RegisterInput
	if err := c.ShouldBindJSON(&params); err != nil {
		response.ErrorResponse(c, response.ErrCodeBindRegisterInput, err.Error())
		return
	}

	codeStatus, err := service.UserLogin().Register(c, &params)
	if codeStatus == response.ErrCodeUserHasExist {
		response.SuccessResponse(c, codeStatus, response.GetMessageCode(codeStatus))
		return
	}
	if codeStatus == response.ErrCodeOTPNotExist {
		response.SuccessResponse(c, codeStatus, response.GetMessageCode(codeStatus))
		return
	}
	if err != nil {
		global.Logger.Error("Error registering user otp", zap.Error(err))
		response.ErrorResponse(c, codeStatus, err.Error())
		return
	}

	response.SuccessResponse(c, response.ErrCodeSuccess, nil)
}

// Verify login by user
// @Summary      Verify OTP bu user when register
// @Description  Verify OTP bu user when register
// @Tags         accounts management
// @Accept       json
// @Produce      json
// @Param        payload body model.VerifyInput true "payload"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /api/v1/user/verify_account [post]
func (cU *cUserLogin) VerifyOTP(c *gin.Context) {
	var params model.VerifyInput
	if err := c.ShouldBindJSON(&params); err != nil {
		response.ErrorResponse(c, response.ErrCodeBindVerifyInput, err.Error())
		return
	}

	out, err := service.UserLogin().VerifyOTP(c, &params)
	if err != nil {
		global.Logger.Error("Error verifying user otp", zap.Error(err))
		response.SuccessResponse(c, response.ErrCodeVerifyOTPFail, err.Error())
		return
	}

	response.SuccessResponse(c, response.ErrCodeSuccess, out)
}

// Update password when register
// @Summary      update password when register
// @Description  after verification otp can be updated password
// @Tags         accounts management
// @Accept       json
// @Produce      json
// @Param        payload body model.UpdatePasswordInput true "payload"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /api/v1/user/upgrade_password_register [post]
func (cU *cUserLogin) UpgradePasswordRegister(c *gin.Context) {
	var params model.UpdatePasswordInput
	if err := c.ShouldBindJSON(&params); err != nil {
		response.ErrorResponse(c, response.ErrCodeBindUpdatePasswordInput, err.Error())
		return
	}

	out, err := service.UserLogin().UpdatePasswordRegister(c, &params)
	if err != nil {
		global.Logger.Error("Error updating password register", zap.Error(err))
		response.ErrorResponse(c, response.ErrCodeUpdatePasswordRegister, err.Error())
		return
	}

	response.SuccessResponse(c, response.ErrCodeSuccess, out)
}

// @Summary      Refresh token
// @Description  Refresh token for user
// @Tags         accounts management
// @Accept       json
// @Produce      json
// @Param        payload body model.RefreshTokenInput true "payload"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /api/v1/user/refresh_token [post]
func (cU *cUserLogin) RefreshToken(c *gin.Context) {
	var parameters model.RefreshTokenInput
	if err := c.ShouldBindJSON(&parameters); err != nil {
        response.ErrorResponse(c, response.ErrCodeBindVerifyInput, err.Error())
        return
    }
	// call to service
	codeRes, out, err := service.UserLogin().RefreshToken(c, &parameters)
    if codeRes != response.ErrCodeSuccess {
		response.ErrorResponse(c, codeRes, response.GetMessageCode(codeRes))
        return
	}
	if err != nil {
        global.Logger.Error("Error refreshing token", zap.Error(err))
        response.ErrorResponse(c, response.ErrCodeRefreshTokenFail, err.Error())
        return
    }

    response.SuccessResponse(c, response.ErrCodeSuccess, out)
}


// @Summary      Forgot Password
// @Description  Forgot Password
// @Tags         accounts management
// @Accept       json
// @Produce      json
// @Param        mail query string true "Mail address"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /api/v1/user/forgot_password [post]
func (cU *cUserLogin) ForgotPassword(c *gin.Context) {
	// query email address
	mail := c.Query("mail")
    if mail == "" {
        response.ErrorResponse(c, response.ErrCodeBadRequest, "Email is required")
        return
    }

    // call to service
    codeRes, err := service.UserLogin().ForgotPassword(c, mail)
    if codeRes != response.ErrCodeSuccess {
		global.Logger.Error("Error forgot password", zap.Error(err))
        response.ErrorResponse(c, codeRes, response.GetMessageCode(codeRes))
        return
    }
	if err != nil {
        global.Logger.Error("Error forgot password", zap.Error(err))
        response.ErrorResponse(c, response.ErrCodeForgotPasswordFail, err.Error())
        return
    }

    response.SuccessResponse(c, response.ErrCodeSuccess, nil)
}

// @Summary      Logout
// @Description  Logout
// @Tags         accounts management
// @Accept       json
// @Produce      json
// @Param        payload body model.LogoutInput true "payload"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /api/v1/user/logout [post]
func (cU *cUserLogin) Logout(c *gin.Context) {
	var parameters model.LogoutInput
	if err := c.ShouldBindJSON(&parameters); err != nil {
		response.ErrorResponse(c, response.ErrCodeBindVerifyInput, err.Error())
		return
	}
	// call to service
	codeRes, err := service.UserLogin().Logout(c, &parameters)
	if codeRes != response.ErrCodeSuccess {
		global.Logger.Error("Error logout user", zap.Error(err))
		response.ErrorResponse(c, codeRes, response.GetMessageCode(codeRes))
		return
	}
	if err != nil {
		global.Logger.Error("Error logout user", zap.Error(err))
		response.ErrorResponse(c, codeRes, err.Error())
		return
	}
	
    response.SuccessResponse(c, response.ErrCodeSuccess, nil)
}

// VerifyForgotPassword
// @Summary      Verify Forgot Password
// @Description  Verify Forgot Password with token
// @Tags         accounts management
// @Accept       json
// @Produce      json
// @Param 	     token path string true "token"
// @Param        email path string true "email"
// @Success      200  {object}  response.ResponseData
// @Failure      500  {object}  response.ErrResponseData
// @Router       /api/v1/user/verify_forgot_password/{email}/{token} [get]
func (cU *cUserLogin) VerifyForgotPassword(c *gin.Context) {
	// get token and email from path
	token := c.Param("token")
	email := c.Param("email")
	if token == "" || email == "" {
		response.ErrorResponse(c, response.ErrCodeBadRequest, "Token and email are required")
		return
	}
	// validate token and email
	if len(token) < 8 {
		response.ErrorResponse(c, response.ErrCodeBadRequest, "Invalid token")
		return
	}
	emailRegex := regexp.MustCompile(`^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$`)
	if !emailRegex.MatchString(email) {
		response.ErrorResponse(c, response.ErrCodeBadRequest, "Invalid email")
		return
	}

	input := model.VerifyForgotPasswordInput{
		Token: token,
		Email: email,
	}
	// call to service
	codeRes, err := service.UserLogin().VerifyForgotPassword(c, &input)
	if codeRes != response.ErrCodeSuccess {
		global.Logger.Error("Error verify forgot password", zap.Error(err))
		response.ErrorResponse(c, codeRes, response.GetMessageCode(codeRes))
		return
	}
	if err != nil {
		global.Logger.Error("Error verify forgot password", zap.Error(err))
		response.ErrorResponse(c, codeRes, err.Error())
		return
	}

    response.SuccessResponse(c, response.ErrCodeSuccess, nil)
}