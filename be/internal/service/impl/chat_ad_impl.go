package impl

import (
	"context"
	"example.com/be/internal/database"
	"example.com/be/internal/model"
	"example.com/be/internal/service"
)

// type chat admin impl
type sChatAdmin struct {
	r *database.Queries
}

// ChangeAdminGroupChat implements service.IChatServiceAdmin.
func (s *sChatAdmin) ChangeAdminGroupChat(ctx context.Context, in *model.ChangeAdminGroupChatInput) (codeResult int, err error) {
	panic("unimplemented") // TODO: cmp
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
