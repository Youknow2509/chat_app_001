package context

import (
	"context"
	"database/sql"
	"errors"
	"strings"

	"example.com/be/internal/consts"
	"example.com/be/internal/utils/cache"
)

type InfoUserUUID struct {
	UserId             string
	UserAccount        string
	UserState          string
	UserIsRefreshToken sql.NullInt32
}

/**
 * Get UUID in header contact after authorization middleware
 * (middleware add new parameter in header)
 */
func GetSubjectUUID(ctx context.Context) (string, error) {
	sUUID, ok := ctx.Value(consts.PAYLOAD_SUBJECT_UUID).(string)
	if !ok {
		return "", errors.New("uuid not found in context")
	}
	return sUUID, nil
}

/**
 * Get USER_ID in header contact after authorization middleware
 * (middleware add new parameter in header)
 */
func GetUserID(ctx context.Context) (string, error) {
	sUUID, ok := ctx.Value(consts.PAYLOAD_USER_ID).(string)
	if !ok {
		return "", errors.New("user id not found in context")
	}
	return sUUID, nil
}

/**
 *  Get userID from context in header field
 *  Header field add new parameter when auth middleware
 */
func GetUserIdFromToken(ctx context.Context) (string, error) {
	userID, err := GetUserID(ctx)
	if err != nil {
		return "", err
	}
	if userID == "" {
		return "", errors.New("user id not found in context")
	}
	// user id 
	userID = strings.TrimSpace(userID)

	return userID, nil
}

/**
 * Get user info from UUID
 */
func GetUserInfoFromUUID(ctx context.Context) (*InfoUserUUID, error) {
	sUUID, err := GetSubjectUUID(ctx)
	if err != nil {
		return nil, err
	}
	// get infoUser Redis from uuid
	var userInfo InfoUserUUID
	if err := cache.GetCache(ctx, sUUID, &userInfo); err != nil {
		return nil, err
	}

	return &userInfo, nil
}