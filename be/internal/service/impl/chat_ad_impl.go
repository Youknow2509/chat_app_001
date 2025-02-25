package impl

import (
	"context"
	"fmt"

	"example.com/be/global"
	"example.com/be/internal/database"
	"example.com/be/internal/model"
	"example.com/be/internal/service"
	"example.com/be/response"
	"go.uber.org/zap"
)

// type chat admin impl
type sChatAdmin struct {
	r *database.Queries
}

// ChangeAdminGroupChat implements service.IChatServiceAdmin.
func (s *sChatAdmin) ChangeAdminGroupChat(ctx context.Context, in *model.ChangeAdminGroupChatInput) (codeResult int, err error) {
	// 1. check user admin group chat
	cAdminGroupChat, err := s.r.CheckAdminGroupChat(ctx, database.CheckAdminGroupChatParams{
		ChatID: in.GroupChatID,
		UserID: in.OldAdminID,
	})
	if err != nil {
		global.Logger.Error("Err checking admin group chat", zap.Error(err))
		return response.ErrCodeChangeAdminChat, err
	}
	if cAdminGroupChat < 1 {
		global.Logger.Error("User not admin group chat", zap.Error(err))
        return response.ErrCodeChangeAdminChat, fmt.Errorf("user admin %s is not admin group chat or group chat %s don't exist", in.OldAdminID, in.GroupChatID)
    }
	// 2. check user admin new in chat
	cUserNewAdmin, err := s.r.CheckUserInGroupChat(ctx, database.CheckUserInGroupChatParams{
		ChatID: in.GroupChatID,
		UserID: in.NewAdminID,
	})
	if err != nil {
		global.Logger.Error("Err checking user in group chat", zap.Error(err))
		return response.ErrCodeChangeAdminChat, err
	}
	if cUserNewAdmin < 1 {
		global.Logger.Error("User not in group chat", zap.Error(err))
		return response.ErrCodeChangeAdminChat, fmt.Errorf("user %s is not in group chat", in.NewAdminID)
	}
	// 3. handle change admin group chat
	go func() {
		err = s.r.ChangeGroupAdmin(ctx, database.ChangeGroupAdminParams{
			ChatID: in.GroupChatID,
			UserID: in.NewAdminID,
		})
		if err != nil {
			fmt.Printf("Err changing admin group chat: %v\n", err)
		}
	}()
	
	// 4. change admin to user
	go func() {
		err = s.r.ChangeToMember(ctx, database.ChangeToMemberParams{
			UserID: in.OldAdminID,
			ChatID: in.GroupChatID,
		})
		if err != nil {
			fmt.Printf("Err changing admin to user: %v\n", err)
		}
	}()
	return response.ErrCodeSuccess, nil
}

// DelChat implements service.IChatServiceAdmin.
func (s *sChatAdmin) DelChat(ctx context.Context, in *model.DelChatInput) (codeResult int, err error) {
	panic("unimplemented") // TODO: cmp
}

// DelMenForChat implements service.IChatServiceAdmin.
func (s *sChatAdmin) DelMenForChat(ctx context.Context, in *model.DelMenForChatInput) (codeResult int, err error) {
	panic("unimplemented") // TODO: cmp
}

// UpgradeChatInfo implements service.IChatServiceAdmin.
func (s *sChatAdmin) UpgradeChatInfo(ctx context.Context, in *model.UpgradeChatInfoInput) (codeResult int, err error) {
	panic("unimplemented") // TODO: cmp
}

// init chat admin service impl IChatServiceAdmin
func NewSChatAdmin(r *database.Queries) service.IChatServiceAdmin {
	return &sChatAdmin{r: r}
}
