package impl

import (
	"context"
	"database/sql"
	"encoding/json"
	"errors"
	"fmt"
	"strconv"
	"strings"
	"time"

	"example.com/be/global"
	"example.com/be/internal/consts"
	"example.com/be/internal/database"
	"example.com/be/internal/model"
	"example.com/be/internal/service"
	"example.com/be/internal/utils"
	"example.com/be/response"
	"github.com/redis/go-redis/v9"
	"go.uber.org/zap"
)

// type chat admin impl
type sChatAdmin struct {
	r *database.Queries
}

// GetUserInChatAdmin implements service.IChatServiceAdmin.
func (s *sChatAdmin) GetUserInChatAdmin(ctx context.Context, in *model.InputGetUserInChatAdmin) (out *model.GetUserInChatOutput, err error) {
	// 1. Check chat group is exist
	chatGroupInfo, err := s.r.GetGroupInfo(ctx, in.ChatID)
	if err != nil {
		fmt.Printf("Err get chat info %s", in.ChatID)
		global.Logger.Error("Err get chat info", zap.Error(err))
		return nil, err
	}
	if chatGroupInfo.Groupid == "" {
		global.Logger.Error("Chat group is not exist")
		return nil, nil
	}
	// 2. check datastore in cache
	keyCache := fmt.Sprintf("listuser::chat%s::l%s::p%s", in.ChatID, strconv.Itoa(in.Limit), strconv.Itoa(in.Page))
	dataUserInChatCache, err := global.Rdb.Get(ctx, keyCache).Result()
	// Check handle get otp in redis - TODO handle utils...
	switch {
	case errors.Is(err, redis.Nil):
		fmt.Println("key does not exist")
	case err != nil:
		fmt.Println("get failed:: ", err)
		return nil, err
	}
	if dataUserInChatCache != "" {
		err = json.Unmarshal([]byte(dataUserInChatCache), &out)
		if err != nil {
			fmt.Printf("Err unmarshal data %s", dataUserInChatCache)
			global.Logger.Error("Err unmarshal data", zap.Error(err))
			return nil, err
		}
		return out, nil
	} else {
		// 3. get user in chat
		dataUserInChat, err := s.r.GetUsersInChat(ctx, database.GetUsersInChatParams{
			ChatID: in.ChatID,
			Limit:  int32(in.Limit),
			Offset: int32(utils.GetOffsetWithLimit(in.Page, in.Limit)),
		})
		if err != nil {
			fmt.Printf("Err get user in chat %s", in.ChatID)
			global.Logger.Error("Err get user in chat", zap.Error(err))
			return nil, err
		}
		// 4. set data to output
		listUser := make([]model.UserInfoBase, len(dataUserInChat))
		for index, v := range dataUserInChat {
			// out.ListUserID = append(out.ListUserID, v.UserID)
			listUser[index] = model.UserInfoBase{
				UserID:       v.UserID,
				UserAccount:  v.UserEmail.String,
				UserNickname: v.UserNickname.String,
				UserAvatar:   v.UserAvatar.String,
			}
		}
		out = &model.GetUserInChatOutput{
			ChatID: in.ChatID,
			Users:  listUser,
		}
		// 6. save to cache
		go func() {
			cacheData, err := json.Marshal(out)
			if err != nil {
				fmt.Printf("Err marshal data %s", out)
				return
			}
			err = global.Rdb.Set(ctx, keyCache, cacheData, time.Duration(consts.TIME_SAVE_CACHE_OFTEN_USE)*time.Minute).Err()
			if err != nil {
				fmt.Println("set failed:: ", err)
				return
			}
		}()
	}

	return out, nil
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
	// 1. check user admin group chat
	cAdminGroupChat, err := s.r.CheckAdminGroupChat(ctx, database.CheckAdminGroupChatParams{
		ChatID: in.ChatID,
		UserID: in.AdminChatID,
	})
	if err != nil {
		global.Logger.Error("Err checking admin group chat", zap.Error(err))
		return response.ErrCodeDelChat, err
	}
	if cAdminGroupChat < 1 {
		global.Logger.Error("User not admin group chat", zap.Error(err))
		return response.ErrCodeDelChat, fmt.Errorf("user admin %s is not admin group chat or group chat %s don't exist", in.AdminChatID, in.ChatID)
	}
	// 2. check chat
	cChat, err := s.r.GetGroupInfo(ctx, in.ChatID)
	if err != nil {
		global.Logger.Error("Err getting group info", zap.Error(err))
		return response.ErrCodeDelChat, err
	}
	if cChat.Groupid == "" {
		global.Logger.Error("Chat not found", zap.Error(err))
		return response.ErrCodeDelChat, fmt.Errorf("chat %s not found", in.ChatID)
	}
	// 3. delete menber in chat
	go func() {
		listIDMenber := strings.Split(cChat.ListMem.String, ",")
		for _, menberID := range listIDMenber {
			err = s.r.DeleteMemberFromChat(context.Background(), database.DeleteMemberFromChatParams{
				ChatID: in.ChatID,
				UserID: menberID,
			})
			if err != nil {
				fmt.Printf("Err deleting member %s from chat %s: %v\n", menberID, in.ChatID, err)
			}
		}
	}()
	// 4. delete chat
	go func() {
		err = s.r.DeleteChat(ctx, in.ChatID)
		if err != nil {
			fmt.Printf("Err deleting chat %s: %v\n", in.ChatID, err)
		}
	}()
	// TODO 5. delete cache chat
	return response.ErrCodeSuccess, nil
}

// DelMenForChat implements service.IChatServiceAdmin.
func (s *sChatAdmin) DelMenForChat(ctx context.Context, in *model.DelMenForChatInput) (codeResult int, err error) {
	// 1. check user admin group chat
	cAdminGroupChat, err := s.r.CheckAdminGroupChat(ctx, database.CheckAdminGroupChatParams{
		ChatID: in.ChatID,
		UserID: in.AdminChatID,
	})
	if err != nil {
		global.Logger.Error("Err checking admin group chat", zap.Error(err))
		return response.ErrCodeDelMenFromChat, err
	}
	if cAdminGroupChat < 1 {
		global.Logger.Error("User not admin group chat", zap.Error(err))
		return response.ErrCodeDelMenFromChat, fmt.Errorf("user admin %s is not admin group chat or group chat %s don't exist", in.AdminChatID, in.ChatID)
	}
	// 2. check user in chat
	cUserInChat, err := s.r.CheckUserInGroupChat(ctx, database.CheckUserInGroupChatParams{
		ChatID: in.ChatID,
		UserID: in.UserDelID,
	})
	if err != nil {
		global.Logger.Error("Err checking user in group chat", zap.Error(err))
		return response.ErrCodeDelMenFromChat, err
	}
	if cUserInChat < 1 {
		global.Logger.Error("User not in group chat", zap.Error(err))
		return response.ErrCodeDelMenFromChat, fmt.Errorf("user %s is not in group chat", in.UserDelID)
	}
	// 3. delete member in chat
	go func() {
		err = s.r.DeleteMemberFromChat(ctx, database.DeleteMemberFromChatParams{
			ChatID: in.ChatID,
			UserID: in.UserDelID,
		})
		if err != nil {
			fmt.Printf("Err deleting member %s from chat %s: %v\n", in.UserDelID, in.ChatID, err)
		}
	}()
	// 4. delete cache chat
	go func() {
		key := fmt.Sprintf("listchat::user%s::", in.UserDelID)
		err = utils.DeleteCacheWithKeyPrefix(key)
		if err != nil {
			fmt.Printf("Err deleting cache chat %s: %v\n", in.ChatID, err)
		}
	}()
	return response.ErrCodeSuccess, nil
}

// UpgradeChatInfo implements service.IChatServiceAdmin.
func (s *sChatAdmin) UpgradeChatInfo(ctx context.Context, in *model.UpgradeChatInfoInput) (codeResult int, err error) {
	// 1. check user admin permissions
	cAdminGroupChat, err := s.r.CheckAdminGroupChat(ctx, database.CheckAdminGroupChatParams{
		ChatID: in.GroupChatID,
		UserID: in.UserAdminID,
	})
	if err != nil {
		global.Logger.Error("Err checking admin group chat", zap.Error(err))
		return response.ErrCodeUpgradeChatInfo, err
	}
	if cAdminGroupChat < 1 {
		global.Logger.Error("User not admin group chat", zap.Error(err))
		return response.ErrCodeUpgradeChatInfo, fmt.Errorf("user admin %s is not admin group chat or group chat %s don't exist", in.UserAdminID, in.GroupChatID)
	}
	// 2. UpdateGroupChat
	go func() {
		err = s.r.UpdateGroupChat(ctx, database.UpdateGroupChatParams{
			GroupName:   sql.NullString{String: in.GroupNameUpdate, Valid: true},
			GroupAvatar: sql.NullString{String: in.GroupAvatar, Valid: true},
			ID:          in.GroupChatID,
		})
		if err != nil {
			fmt.Printf("Err updating group chat %s: %v\n", in.GroupChatID, err)
		}
	}()

	// 3. delete cache chat info
	go func() {
		key := fmt.Sprintf("chatinfo::%s", in.GroupChatID)
		err = global.Rdb.Del(context.Background(), key).Err()
		if err != nil {
			fmt.Printf("Err deleting cache chat info %s: %v\n", in.GroupChatID, err)
		}
	}()
	return response.ErrCodeSuccess, nil
}

// init chat admin service impl IChatServiceAdmin
func NewSChatAdmin(r *database.Queries) service.IChatServiceAdmin {
	return &sChatAdmin{r: r}
}
