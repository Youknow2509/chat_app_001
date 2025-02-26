package response

const (
	ErrCodeSuccess      = 20001 // Success,
	ErrCodeParamInvalid = 20003 // Email is invalid

	ErrInvalidToken = 30001 // Invalid token
	ErrInvalidOTP   = 30002 // Invalid otp
	ErrSendEmailOTP = 30003 // Send email failed

	ErrCodeAuthFailed = 401 // Auth failed

	ErrCodeUnmarshalData = 40001 // Unmarshal data failed

	ErrCodeInvalidInput = 40002 // Invalid input
	ErrCodeBadRequest   = 40003 // Bad request

	// Register Code
	ErrCodeUserHasExist            = 50001 // User has exist
	ErrCodeBindRegisterInput       = 50002
	ErrCodeBindVerifyInput         = 50003
	ErrCodeVerifyOTPFail           = 50004
	ErrCodeBindUpdatePasswordInput = 50005
	ErrCodeBindLoginInput          = 50006

	// Login Code
	ErrCodeOTPNotExist = 60001

	ErrCodeUserOTPNotExist = 60002
	ErrCodeOTPDontVerify   = 60003

	ErrCodeUpdatePasswordRegister = 100000

	// crypto code
	ErrCodeCryptoHash    = 70001
	ErrCodeGeneratorSalt = 70002

	// database code
	ErrCodeAddUserBase      = 80001
	ErrCodeQueryUserBase    = 80002
	ErrCodeUpdateUserBase   = 80003
	ErrCodeDeleteUserBase   = 80004
	ErrCodeUserBaseNotFound = 80005

	ErrCodeAddUserInfo  = 90001
	ErrCodeUserNotFound = 90002
	ErrCodeDeleteCache  = 90003

	// two factor authentication code
	ErrCodeTwoFactorAuthSetupFailed = 9002
	ErrCodeTwoFactorAuthFailed      = 9003
	ErrCodeUnauthorized             = 9004

	// rate limit code
	ErrCodeTooManyRequests = 429

	// token
	ErrCodeCreateToken        = 100001
	ErrCodeCreateRefreshToken = 100002
	ErrCodeTokenExpired       = 100003
	ErrCodeTokenInvalid       = 100004
	ErrCodeBindTokenInput     = 100005

	// chat
	ErrCodeAddMemberToChat        = 110001
	ErrCodeCreateChatGroup        = 110002
	ErrCodeCreateChatPrivate      = 110003
	ErrCodeGetChatInfo            = 110004
	ErrCodeGetListChat            = 110005
	ErrCodeGetUserInChat          = 110006
	ErrCodeGetListChatForUser     = 110007
	ErrCodeChatPrivateExists      = 110008
	ErrCodeCreateChatGroupSuccess = 110009
	ErrCodeCheckUserInChat        = 110010
	ErrCodeChangeAdminChat        = 110011
	ErrCodeDelMenFromChat         = 110012
	ErrCodeDelChat                = 110013
	ErrCodeUpgradeChatInfo        = 110014

	// user
	ErrCodeCheckFriendRequest        = 120000
	ErrCodeFrinedRequestNotFound     = 120001
	ErrCodeUserBlockAddFriendRequest = 120002
	ErrCodeGetUserInfo               = 120003
	ErrCodeFindUser                  = 120004
	ErrCodeUpdateUserInfo            = 120005
	ErrCodeCreateFriendRequest       = 120006
	ErrCodeEndFriendRequest          = 120007
	ErrCodeDeleteFriend              = 120008
	ErrCodeGetListFriendRequest      = 120009
)

// message
var msg = map[int]string{
	ErrCodeGetListFriendRequest:     "get list friend request failed",
	ErrCodeDeleteFriend:             "delete friend failed",
	ErrCodeEndFriendRequest:         "best failed",
	ErrCodeCreateFriendRequest:      "create friend request failed",
	ErrCodeUpdateUserInfo:           "update user info failed",
	ErrCodeFindUser:                 "find user failed",
	ErrCodeBadRequest:               "bad request",
	ErrCodeGetUserInfo:              "get user info failed",
	ErrCodeFrinedRequestNotFound:    "frined request not found",
	ErrCodeCheckFriendRequest:       "check friend request failed",
	ErrCodeUpgradeChatInfo:          "upgrade chat info failed",
	ErrCodeDelMenFromChat:           "delete member from chat failed",
	ErrCodeDelChat:                  "del chat failed",
	ErrCodeChangeAdminChat:          "change admin chat failed",
	ErrCodeInvalidInput:             "invalid input",
	ErrCodeCheckUserInChat:          "check user in chat failed",
	ErrCodeCreateChatGroupSuccess:   "create chat group success",
	ErrCodeChatPrivateExists:        "chat private exists",
	ErrCodeUnauthorized:             "unauthorized",
	ErrCodeUnmarshalData:            "unmarshal data failed",
	ErrCodeDeleteCache:              "delete cache failed",
	ErrCodeGetListChatForUser:       "get list chat for user failed",
	ErrCodeGetUserInChat:            "get user in chat failed",
	ErrCodeGetListChat:              "get list chat failed",
	ErrCodeGetChatInfo:              "get chat info failed",
	ErrCodeCreateChatPrivate:        "create chat private failed",
	ErrCodeCreateChatGroup:          "create chat group failed",
	ErrCodeAddMemberToChat:          "add member to chat failed",
	ErrCodeSuccess:                  "success",
	ErrCodeParamInvalid:             "email is invalid",
	ErrInvalidToken:                 "token is invalid",
	ErrInvalidOTP:                   "otp is invalid",
	ErrSendEmailOTP:                 "send email otp failed",
	ErrCodeUserHasExist:             "user has exist",
	ErrCodeBindRegisterInput:        "bind register input failed",
	ErrCodeBindVerifyInput:          "bind verify input failed",
	ErrCodeVerifyOTPFail:            "verify otp failed",
	ErrCodeBindUpdatePasswordInput:  "bind update password input failed",
	ErrCodeOTPNotExist:              "otp exists but not registered",
	ErrCodeUserOTPNotExist:          "user otp does not exist",
	ErrCodeOTPDontVerify:            "otp does not verify",
	ErrCodeCryptoHash:               "crypto hash failed",
	ErrCodeGeneratorSalt:            "generator salt failed",
	ErrCodeAddUserBase:              "add user base failed",
	ErrCodeQueryUserBase:            "query user base failed",
	ErrCodeUpdateUserBase:           "update user base failed",
	ErrCodeDeleteUserBase:           "delete user base failed",
	ErrCodeUserBaseNotFound:         "user base not found",
	ErrCodeAddUserInfo:              "add user info failed",
	ErrCodeUpdatePasswordRegister:   "update password register failed",
	ErrCodeUserNotFound:             "user not found",
	ErrCodeAuthFailed:               "auth failed",
	ErrCodeBindLoginInput:           "bind login input failed",
	ErrCodeTwoFactorAuthSetupFailed: "two factor authentication setup failed",
	ErrCodeTwoFactorAuthFailed:      "two factor authentication failed",
	ErrCodeTooManyRequests:          "too many requests",
	ErrCodeCreateToken:              "create token failed",
	ErrCodeCreateRefreshToken:       "create refresh token failed",
	ErrCodeTokenExpired:             "token expired",
	ErrCodeTokenInvalid:             "token invalid",
	ErrCodeBindTokenInput:           "bind token input failed",
}

// get message code
func GetMessageCode(code int) string {
	return msg[code]
}
