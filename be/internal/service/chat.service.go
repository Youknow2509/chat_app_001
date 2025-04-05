package service

import (
	"context"

	"example.com/be/internal/model"
)

// create interface
type (
	IChatService interface {
		CreateChatGroup(ctx context.Context, in *model.CreateChatGroupInput) (codeResult int, out *model.OutputCreateChatGroup, err error)
		CreateChatPrivate(ctx context.Context, in *model.CreateChatPrivateInput) (codeResult int, out *model.OutputCreateChatGroup, err error)
		GetChatInfo(ctx context.Context, in *model.InputGetChatInfor) (out *model.ChatInfoOutput, err error)
		GetListChatForUser(ctx context.Context, in *model.InputGetChatForUser) (out []*model.OutGetListChatForUser, codeResult int, err error)
		UserGetListChatPrivateForUser(ctx context.Context, in *model.InputGetChatForUser) (out []*model.UserChatItemOutput, codeResult int, err error)
		UserGetListChatGroupForUser(ctx context.Context, in *model.InputGetChatForUser) (out []*model.UserChatItemOutput, codeResult int, err error)
		GetUserInChat(ctx context.Context, in *model.InputGetUserInChat) (out *model.GetUserInChatOutput, err error)
		AddMemberToChat(ctx context.Context, in *model.AddMemberToChatInput) (codeResult int, out *model.AddMemberToChatOutput, err error)
	}

	IChatServiceAdmin interface {
		GetUserInChatAdmin(ctx context.Context, in *model.InputGetUserInChatAdmin) (out *model.GetUserInChatOutput, err error)
		UpgradeChatInfo(ctx context.Context, in *model.UpgradeChatInfoInput) (codeResult int, err error)
		ChangeAdminGroupChat(ctx context.Context, in *model.ChangeAdminGroupChatInput) (codeResult int, err error)
		DelMenForChat(ctx context.Context, in *model.DelMenForChatInput) (codeResult int, err error)
		DelChat(ctx context.Context, in *model.DelChatInput) (codeResult int, err error)
	}
)

// variables for service interface
var (
	localChat IChatService
	localChatAdmin IChatServiceAdmin
)

/**
 * Handle interface IChatService
 */
// Get interface IChatService
func ChatService() IChatService {
	if localChat == nil {
		panic("implement localChat not found for interface IUserLogin")
	}
	return localChat
}

// Init interface IChatService
func InitChatService(chatS IChatService) {
	localChat = chatS
}

/**
 * Handle interface IChatServiceAdmin
 */
// Get interface IChatServiceAdmin
func ChatServiceAdmin() IChatServiceAdmin {
	if localChatAdmin == nil {
		panic("implement localChatAdmin not found for interface IUserLogin")
	}
	return localChatAdmin
}

// Init interface IChatServiceAdmin
func InitChatServiceAdmin(chatSAdmin IChatServiceAdmin) {
    localChatAdmin = chatSAdmin
}
