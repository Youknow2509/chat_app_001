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
	"github.com/google/uuid"
	"github.com/redis/go-redis/v9"
	"go.uber.org/zap"
)

// type chat base service
type sChatBase struct {
	r *database.Queries
}

// GetChatInfo implements service.IChatService.
func (s *sChatBase) GetChatInfo(ctx context.Context, in *model.InputGetChatInfor) (out *model.ChatInfoOutput, err error) {
	// 1. Check chat info exists
	chatInfo, err := s.r.GetGroupInfo(ctx, in.ChatID)
	if err != nil {
		fmt.Printf("Err get chat info %s", in.ChatID)
		global.Logger.Error("Err get chat info", zap.Error(err))
		return nil, err
	}
	if chatInfo.Groupid == "" {
		global.Logger.Error("Chat group is not exist")
		return nil, nil
	}
	// 2. check role user or user access in chat info
	cUserInChat, err := s.r.CheckUserInChat(ctx, database.CheckUserInChatParams{
		ID:    in.ChatID,
		UserID: in.UserID,
	})
	if err != nil {
		fmt.Printf("Err check user in chat %s", in.ChatID)
		global.Logger.Error("Err check user in chat", zap.Error(err))
		return nil, err
	}
	if cUserInChat < 1 {
		global.Logger.Error("User is not in chat")
		return nil, fmt.Errorf("user is not in chat")
	}
	// 3. Get data in cache
	keyCache := fmt.Sprintf("chatinfo::%s", in.ChatID)
	dataCache, err := global.Rdb.Get(ctx, keyCache).Result()
	// Check handle get data in redis - TODO handle utils...
	switch {
	case errors.Is(err, redis.Nil):
		fmt.Println("key does not exist")
	case err != nil:
		fmt.Println("get failed:: ", err)
		return nil, err
	}
	if dataCache != "" {
		err = json.Unmarshal([]byte(dataCache), &out)
		if err != nil {
			fmt.Printf("Err unmarshal data %s", dataCache)
			global.Logger.Error("Err unmarshal data", zap.Error(err))
			return nil, err
		}
		return out, nil
	} else {
		// 3. If not in cache, get data from database
		dataChatInfo, err := s.r.GetGroupInfo(ctx, in.ChatID)
		if err != nil {
			fmt.Printf("Err get chat info %s", in.ChatID)
			global.Logger.Error("Err get chat info", zap.Error(err))
			return nil, err
		}
		out = &model.ChatInfoOutput{
			ChatID:         dataChatInfo.Groupid,
			ChatName:       dataChatInfo.GroupName.String,
			TypeChat:       dataChatInfo.ChatType,
			NumberofMember: int(dataChatInfo.Numberofmember),
			ListId:         strings.Split(dataChatInfo.ListMem.String, ","),
			Avatar:         dataChatInfo.ChatAvatar.String,
		}
		// 4. Set data to cache
		go func() {
			err = global.Rdb.Set(ctx, keyCache, out, time.Duration(consts.TIME_SAVE_CACHE_OFTEN_USE)*time.Minute).Err()
			if err != nil {
				fmt.Println("set failed:: ", err)
			}
		}()
	}

	return out, nil
}

// GetListChatForUser implements service.IChatService.
func (s *sChatBase) GetListChatForUser(ctx context.Context, in *model.InputGetChatForUser) (out []*model.OutGetListChatForUser, codeResult int, err error) {
	// 1. check user is exist
	userInfo, err := s.r.GetUserWithID(ctx, in.UserID)
	if err != nil {
		fmt.Printf("Err get user info %s", in.UserID)
		global.Logger.Error("Err get user info", zap.Error(err))
		return nil, response.ErrCodeUserNotFound, err
	}
	if userInfo.UserID == "" {
		global.Logger.Error("User is not exist")
		return nil, response.ErrCodeUserNotFound, nil
	}
	keyCache := fmt.Sprintf("listchat::user%s::l%s::p%s", in.UserID, strconv.Itoa(in.Limit), strconv.Itoa(in.Page))
	// 2. check datastore in cache
	dataR, err := global.Rdb.Get(ctx, keyCache).Result()
	// Check handle get otp in redis - TODO handle utils...
	switch {
	case errors.Is(err, redis.Nil):
		fmt.Println("key does not exist")
	case err != nil:
		fmt.Println("get failed:: ", err)
		return nil, response.ErrInvalidOTP, err
	}
	if dataR != "" {
		err = json.Unmarshal([]byte(dataR), &out)
		if err != nil {
			fmt.Printf("Err unmarshal data %s", dataR)
			global.Logger.Error("Err unmarshal data", zap.Error(err))
			return nil, response.ErrCodeUnmarshalData, err
		}

		return out, response.ErrCodeSuccess, nil
	} else {
		// 3. if dont have, get list chat for user
		dataD, err := s.r.GetChatListForUser(ctx, database.GetChatListForUserParams{
			UserID: in.UserID,
			Limit:  int32(in.Limit),
			Offset: int32(utils.GetOffsetWithLimit(in.Page, in.Limit)),
		})
		if err != nil {
			fmt.Printf("Err get list chat for user %s", in.UserID)
			global.Logger.Error("Err get list chat for user", zap.Error(err))
			return nil, response.ErrCodeGetListChatForUser, err
		}
		// 4. set list chat to output
		for _, v := range dataD {
			out = append(out, &model.OutGetListChatForUser{
				ChatID:   v.ChatID,
				ChatName: v.ChatName.String,
				TypeChat: v.ChatType,
				Avatar:   v.ChatAvatar.String,
			})
		}
		// 5. set data to cache
		go func() {
			err = global.Rdb.Set(ctx, keyCache, out, time.Duration(consts.TIME_SAVE_CACHE_OFTEN_USE)*time.Minute).Err()
			if err != nil {
				fmt.Println("set failed:: ", err)
			}
		}()
	}

	return out, response.ErrCodeSuccess, nil
}

// GetUserInChat implements service.IChatService.
func (s *sChatBase) GetUserInChat(ctx context.Context, in *model.InputGetUserInChat) (out *model.GetUserInChatOutput, err error) {
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
		for _, v := range dataUserInChat {
			out.ListUserID = append(out.ListUserID, v.UserID)
		}
		out.ChatID = in.ChatID
		// 5. save to cache
		err = global.Rdb.Set(ctx, keyCache, out, time.Duration(consts.TIME_SAVE_CACHE_OFTEN_USE)*time.Minute).Err()
		if err != nil {
			fmt.Println("set failed:: ", err)
			return nil, err
		}
	}

	return out, nil
}

// AddMemberToChat implements service.IChatService.
func (s *sChatBase) AddMemberToChat(ctx context.Context, in *model.AddMemberToChatInput) (codeResult int, out *model.AddMemberToChatOutput, err error) {
	// 1. Check user is admin group chat
	isUserAdmin, err := s.r.CheckAdminGroupChat(ctx, database.CheckAdminGroupChatParams{
		ChatID: in.ChatID,
		UserID: in.AdminChatID,
	})
	if err != nil {
		global.Logger.Error("Err checking admin group chat", zap.Error(err))
		return response.ErrCodeAuthFailed, nil, err
	}
	if isUserAdmin < 1 {
		global.Logger.Error("User is not admin group chat")
		return response.ErrCodeAuthFailed, nil, nil
	}
	// check chat is type private -> create new chat group with members
	chatInfo, err := s.r.GetGroupInfo(ctx, in.ChatID)
	if err != nil {
		fmt.Printf("Err get chat info %s", in.ChatID)
		global.Logger.Error("Err get chat info", zap.Error(err))
		return response.ErrCodeGetChatInfo, nil, err
	}
	if chatInfo.ChatType == "private" {
		listMemNow := strings.Split(chatInfo.ListMem.String, ",")
		listMemNow = append(listMemNow, in.UserAddID)
		codeRes, out, err := s.CreateChatGroup(ctx, &model.CreateChatGroupInput{
			UserIDCreate: in.AdminChatID,
			GroupName:   chatInfo.GroupName.String,
			ListId:  listMemNow,
		})
		if err != nil {
			global.Logger.Error("Err create chat group", zap.Error(err))
			return codeRes, nil, err
		}
		fmt.Printf("Create chat group %s from %s success", out.ChatId, in.AdminChatID)
		
		return response.ErrCodeSuccess, &model.AddMemberToChatOutput{
			ChatID: out.ChatId,
			TypeAdd: "group",
			ChatName: out.Name,
            Avatar:  chatInfo.ChatAvatar.String,
		}, nil
	} else {
		// 3. handle add new member to group chat
		err = s.r.AddMemberToChat(ctx, database.AddMemberToChatParams{
			ChatID: in.ChatID,
			UserID: in.UserAddID,
		})
		if err != nil {
			global.Logger.Error("Err add member to chat", zap.Error(err))
			return response.ErrCodeAddMemberToChat, nil, err
		}
		fmt.Printf("Add member %s to chat %s success", in.UserAddID, in.ChatID)
	}
	// 4. remove cache data list chat group user
	go func() {
		keyCache := fmt.Sprintf("listchat::user%s", in.UserAddID)
		err = utils.DeleteCacheWithKeyPrefix(keyCache)
		if err != nil {
			fmt.Printf("Err delete cache %s", keyCache)
			global.Logger.Error("Err delete cache", zap.Error(err))
		}
	}()
	return response.ErrCodeSuccess, &model.AddMemberToChatOutput{
		ChatID: in.ChatID,
		TypeAdd: "private",
		ChatName: chatInfo.GroupName.String,
		Avatar:  chatInfo.ChatAvatar.String,
	}, nil
}

// CreateChatGroup implements service.IChatService.
func (s *sChatBase) CreateChatGroup(ctx context.Context, in *model.CreateChatGroupInput) (codeResult int, out *model.OutputCreateChatGroup, err error) {
	// 1. check list members
	if len(in.ListId) < 2 {
		global.Logger.Error("List members must be greater than 2")
		return response.ErrCodeParamInvalid, nil, fmt.Errorf("list members must be greater than 2")
	}
	// 2. create chat group
	uuidGroupChat := uuid.New().String()
	err = s.r.CreateGroupChat(ctx, database.CreateGroupChatParams{
		ID:        uuidGroupChat,
		GroupName: sql.NullString{String: in.GroupName, Valid: true},
	})
	if err != nil {
		global.Logger.Error("Err create group chat", zap.Error(err))
		return response.ErrCodeCreateChatGroup, nil, err
	}
	// 3. add member to chat
	go func() {
		for _, v := range in.ListId {
			// check id equal id user create
			if v == in.UserIDCreate {
				continue
			}
			errAddChatMember := s.r.AddMemberToChat(ctx, database.AddMemberToChatParams{
				ChatID: uuidGroupChat,
				UserID: v,
			})
			if errAddChatMember != nil {
				fmt.Printf("Err add member %s to chat %s", v, uuidGroupChat)
				global.Logger.Error("Err add member to chat", zap.Error(errAddChatMember))
			}
		}
	}()
	// 4. add admin to chat group
	go func() {
		errAddAdminMember := s.r.InsertChatMember(ctx, database.InsertChatMemberParams{
			ChatID: uuidGroupChat,
			UserID: in.UserIDCreate,
			Role:   "admin",
		})
		if errAddAdminMember != nil {
			fmt.Printf("Err add admin member %s to chat %s", in.UserIDCreate, uuidGroupChat)
			global.Logger.Error("Err add admin member to chat", zap.Error(errAddAdminMember))
		}
	}()
	// 5. remove cache data list chat group user
	for _, v := range in.ListId {
		go func() {
			keyCache := fmt.Sprintf("listchat::user%s", v)
			err = utils.DeleteCacheWithKeyPrefix(keyCache)
			if err != nil {
				fmt.Printf("Err delete cache %s", keyCache)
			}
		}()
	}
	keyCacheAdmin := fmt.Sprintf("listchat::user%s", in.UserIDCreate)
	go func() {
		err = utils.DeleteCacheWithKeyPrefix(keyCacheAdmin)
		if err != nil {
			fmt.Printf("Err delete cache %s", keyCacheAdmin)
		}
	}()
	// 6. set data to output
	out = &model.OutputCreateChatGroup{
		ChatId: uuidGroupChat,
		Avatar: "",
		Name:   in.GroupName,
	}
	fmt.Printf("Create chat group %s from %s success", uuidGroupChat, in.UserIDCreate)
	return response.ErrCodeSuccess, out, nil
}

// CreateChatPrivate implements service.IChatService.
func (s *sChatBase) CreateChatPrivate(ctx context.Context, in *model.CreateChatPrivateInput) (codeResult int, out *model.OutputCreateChatGroup, err error) {
	// 1. Check user is exist
	_, err = s.r.GetUserWithID(ctx, in.User1)
	if err != nil {
		fmt.Printf("Err get user with id %s", in.User1)
		global.Logger.Error("Err get user with id", zap.Error(err))
		return response.ErrCodeUserNotFound, nil, err
	}
	_, err = s.r.GetUserWithID(ctx, in.User2)
	if err != nil {
		fmt.Printf("Err get user with id %s", in.User2)
		global.Logger.Error("Err get user with id", zap.Error(err))
		return response.ErrCodeUserNotFound, nil, err
	}
	// 2. check chat private exists
	chatIdExists, err := s.r.CheckPrivateChatExists(ctx, database.CheckPrivateChatExistsParams{
		UserID:   in.User1,
		UserID_2: in.User2,
	})
	if err != nil && err != sql.ErrNoRows {
		fmt.Printf("Err check private chat exists when create chat private")
		global.Logger.Error("Err check private chat exists", zap.Error(err))
		return response.ErrCodeCreateChatPrivate, nil, err
	}
	if chatIdExists != "" {
		global.Logger.Error("Chat private is exist with ID: ", zap.String("chatId", chatIdExists))
		return response.ErrCodeChatPrivateExists, nil, fmt.Errorf("chat private is exist with ID: %s", chatIdExists)
	}

	// 3. create chat private
	groupNameChat := in.User1 + " - " + in.User2
	uuidChatPrivate := uuid.New().String()
	err = s.r.CreateChat(ctx, database.CreateChatParams{
		ID:        uuidChatPrivate,
		GroupName: sql.NullString{String: groupNameChat, Valid: false},
	})
	if err != nil {
		fmt.Printf("Err create chat private %s", uuidChatPrivate)
		global.Logger.Error("Err create chat private", zap.Error(err))
		return response.ErrCodeCreateChatPrivate, nil, err
	}
	// 4. add member to chat
	go func() {
		errAddChatMember1 := s.r.InsertChatMember(ctx, database.InsertChatMemberParams{
			ChatID: uuidChatPrivate,
			UserID: in.User1,
			Role:   "admin",
		})
		if errAddChatMember1 != nil {
			fmt.Printf("Err add member %s to chat %s", in.User1, uuidChatPrivate)
			global.Logger.Error("Err add member to chat", zap.Error(errAddChatMember1))
		}
	}()
	go func() {
		errAddChatMember2 := s.r.InsertChatMember(ctx, database.InsertChatMemberParams{
			ChatID: uuidChatPrivate,
			UserID: in.User2,
			Role:   "admin",
		})
		if errAddChatMember2 != nil {
			fmt.Printf("Err add member %s to chat %s", in.User2, uuidChatPrivate)
			global.Logger.Error("Err add member to chat", zap.Error(errAddChatMember2))
		}
	}()
	// 5. remove cache data list chat group user
	go func() {
		keyCache1 := fmt.Sprintf("listchat::user%s", in.User1)
		err = utils.DeleteCacheWithKeyPrefix(keyCache1)
		if err != nil {
			fmt.Printf("Err delete cache %s", keyCache1)
		}
	}()
	go func() {
		keyCache2 := fmt.Sprintf("listchat::user%s", in.User2)
		err = utils.DeleteCacheWithKeyPrefix(keyCache2)
		if err != nil {
			fmt.Printf("Err delete cache %s", keyCache2)
			global.Logger.Error("Err delete cache", zap.Error(err))
		}
	}()
	// 6. set data to output
	out = &model.OutputCreateChatGroup{
		ChatId: uuidChatPrivate,
		Avatar: "",
		Name:   groupNameChat,
	}
	return response.ErrCodeSuccess, out, nil
}

// new chat base service impl interface chat service
func NewSChatBase(r *database.Queries) service.IChatService {
	return &sChatBase{r: r}
}
