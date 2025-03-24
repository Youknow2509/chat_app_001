package context

import (
	"context"
	"database/sql"
	"errors"

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
 *  Get userID from context in header field
 *  Header field add new parameter when auth middleware
 */
func GetUserIdFromUUID(ctx context.Context) (string, error) {
	sUUID, err := GetSubjectUUID(ctx)
	if err != nil {
		return "", err
	}
	// get infoUser Redis from uuid
	var userInfo InfoUserUUID
	if err := cache.GetCache(ctx, sUUID, &userInfo); err != nil {
		return "", err
	}

	return userInfo.UserId, nil
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