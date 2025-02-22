// Code generated by sqlc. DO NOT EDIT.
// versions:
//   sqlc v1.27.0
// source: user_info_001.sql

package database

import (
	"context"
	"database/sql"
)

const addUserHaveUserId = `-- name: AddUserHaveUserId :execresult
INSERT INTO ` + "`" + `user_info` + "`" + ` (
    user_id, user_account, user_nickname, 
    user_avatar, user_state, user_mobile, 
    user_gender, user_birthday, user_email)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
`

type AddUserHaveUserIdParams struct {
	UserID       string
	UserAccount  string
	UserNickname sql.NullString
	UserAvatar   sql.NullString
	UserState    UserInfoUserState
	UserMobile   sql.NullString
	UserGender   NullUserInfoUserGender
	UserBirthday sql.NullTime
	UserEmail    sql.NullString
}

func (q *Queries) AddUserHaveUserId(ctx context.Context, arg AddUserHaveUserIdParams) (sql.Result, error) {
	return q.db.ExecContext(ctx, addUserHaveUserId,
		arg.UserID,
		arg.UserAccount,
		arg.UserNickname,
		arg.UserAvatar,
		arg.UserState,
		arg.UserMobile,
		arg.UserGender,
		arg.UserBirthday,
		arg.UserEmail,
	)
}

const editUserByUserId = `-- name: EditUserByUserId :execresult
UPDATE ` + "`" + `user_info` + "`" + `
SET user_nickname = ?, user_avatar = ?, user_mobile = ?,
    user_gender = ?, user_birthday = ?, user_email = ?, 
    updated_at = NOW()
WHERE user_id = ?
`

type EditUserByUserIdParams struct {
	UserNickname sql.NullString
	UserAvatar   sql.NullString
	UserMobile   sql.NullString
	UserGender   NullUserInfoUserGender
	UserBirthday sql.NullTime
	UserEmail    sql.NullString
	UserID       string
}

func (q *Queries) EditUserByUserId(ctx context.Context, arg EditUserByUserIdParams) (sql.Result, error) {
	return q.db.ExecContext(ctx, editUserByUserId,
		arg.UserNickname,
		arg.UserAvatar,
		arg.UserMobile,
		arg.UserGender,
		arg.UserBirthday,
		arg.UserEmail,
		arg.UserID,
	)
}

const findUserWithMail = `-- name: FindUserWithMail :many
SELECT user_id, user_account, user_nickname, user_avatar, user_state, user_mobile, user_gender, user_birthday, user_email, user_is_authentication, created_at, updated_at FROM user_info WHERE user_email LIKE ?
ORDER BY user_nickname ASC
LIMIT ? OFFSET ?
`

type FindUserWithMailParams struct {
	UserEmail sql.NullString
	Limit     int32
	Offset    int32
}

func (q *Queries) FindUserWithMail(ctx context.Context, arg FindUserWithMailParams) ([]UserInfo, error) {
	rows, err := q.db.QueryContext(ctx, findUserWithMail, arg.UserEmail, arg.Limit, arg.Offset)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var items []UserInfo
	for rows.Next() {
		var i UserInfo
		if err := rows.Scan(
			&i.UserID,
			&i.UserAccount,
			&i.UserNickname,
			&i.UserAvatar,
			&i.UserState,
			&i.UserMobile,
			&i.UserGender,
			&i.UserBirthday,
			&i.UserEmail,
			&i.UserIsAuthentication,
			&i.CreatedAt,
			&i.UpdatedAt,
		); err != nil {
			return nil, err
		}
		items = append(items, i)
	}
	if err := rows.Close(); err != nil {
		return nil, err
	}
	if err := rows.Err(); err != nil {
		return nil, err
	}
	return items, nil
}

const findUsers = `-- name: FindUsers :many
SELECT user_id, user_account, user_nickname, user_avatar, user_state, user_mobile, user_gender, user_birthday, user_email, user_is_authentication, created_at, updated_at FROM user_info 
WHERE user_account LIKE ? OR user_nickname LIKE ?
ORDER BY user_nickname ASC
LIMIT ? OFFSET ?
`

type FindUsersParams struct {
	UserAccount  string
	UserNickname sql.NullString
	Limit        int32
	Offset       int32
}

func (q *Queries) FindUsers(ctx context.Context, arg FindUsersParams) ([]UserInfo, error) {
	rows, err := q.db.QueryContext(ctx, findUsers,
		arg.UserAccount,
		arg.UserNickname,
		arg.Limit,
		arg.Offset,
	)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var items []UserInfo
	for rows.Next() {
		var i UserInfo
		if err := rows.Scan(
			&i.UserID,
			&i.UserAccount,
			&i.UserNickname,
			&i.UserAvatar,
			&i.UserState,
			&i.UserMobile,
			&i.UserGender,
			&i.UserBirthday,
			&i.UserEmail,
			&i.UserIsAuthentication,
			&i.CreatedAt,
			&i.UpdatedAt,
		); err != nil {
			return nil, err
		}
		items = append(items, i)
	}
	if err := rows.Close(); err != nil {
		return nil, err
	}
	if err := rows.Err(); err != nil {
		return nil, err
	}
	return items, nil
}

const getUserWithAccount = `-- name: GetUserWithAccount :one
SELECT
    user_id, 
    user_account, 
    user_nickname, 
    user_avatar, 
    user_state, 
    user_mobile, 
    user_gender, 
    user_birthday, 
    user_email, 
    created_at, 
    updated_at
FROM ` + "`" + `user_info` + "`" + `
WHERE user_account = ? LIMIT 1
`

type GetUserWithAccountRow struct {
	UserID       string
	UserAccount  string
	UserNickname sql.NullString
	UserAvatar   sql.NullString
	UserState    UserInfoUserState
	UserMobile   sql.NullString
	UserGender   NullUserInfoUserGender
	UserBirthday sql.NullTime
	UserEmail    sql.NullString
	CreatedAt    sql.NullTime
	UpdatedAt    sql.NullTime
}

func (q *Queries) GetUserWithAccount(ctx context.Context, userAccount string) (GetUserWithAccountRow, error) {
	row := q.db.QueryRowContext(ctx, getUserWithAccount, userAccount)
	var i GetUserWithAccountRow
	err := row.Scan(
		&i.UserID,
		&i.UserAccount,
		&i.UserNickname,
		&i.UserAvatar,
		&i.UserState,
		&i.UserMobile,
		&i.UserGender,
		&i.UserBirthday,
		&i.UserEmail,
		&i.CreatedAt,
		&i.UpdatedAt,
	)
	return i, err
}

const getUserWithID = `-- name: GetUserWithID :one
SELECT
    user_info.user_id, 
    user_info.user_account, 
    user_info.user_nickname, 
    user_info.user_avatar, 
    user_info.user_state, 
    user_info.user_mobile, 
    user_info.user_gender, 
    user_info.user_birthday, 
    user_info.user_email, 
    user_base.user_is_refresh_token,
    user_info.created_at, 
    user_info.updated_at
FROM ` + "`" + `user_info` + "`" + ` INNER JOIN ` + "`" + `user_base` + "`" + `
    ON user_base.user_id = user_info.user_id
WHERE user_info.user_id = ? LIMIT 1
`

type GetUserWithIDRow struct {
	UserID             string
	UserAccount        string
	UserNickname       sql.NullString
	UserAvatar         sql.NullString
	UserState          UserInfoUserState
	UserMobile         sql.NullString
	UserGender         NullUserInfoUserGender
	UserBirthday       sql.NullTime
	UserEmail          sql.NullString
	UserIsRefreshToken sql.NullInt32
	CreatedAt          sql.NullTime
	UpdatedAt          sql.NullTime
}

func (q *Queries) GetUserWithID(ctx context.Context, userID string) (GetUserWithIDRow, error) {
	row := q.db.QueryRowContext(ctx, getUserWithID, userID)
	var i GetUserWithIDRow
	err := row.Scan(
		&i.UserID,
		&i.UserAccount,
		&i.UserNickname,
		&i.UserAvatar,
		&i.UserState,
		&i.UserMobile,
		&i.UserGender,
		&i.UserBirthday,
		&i.UserEmail,
		&i.UserIsRefreshToken,
		&i.CreatedAt,
		&i.UpdatedAt,
	)
	return i, err
}

const getUsers = `-- name: GetUsers :many
SELECT 
    user_id, 
    user_account, 
    user_nickname, 
    user_avatar, 
    user_state, 
    user_mobile, 
    user_gender, 
    user_birthday, 
    user_email,
    created_at,
    updated_at
FROM ` + "`" + `user_info` + "`" + `
WHERE user_id IN (?)
`

type GetUsersRow struct {
	UserID       string
	UserAccount  string
	UserNickname sql.NullString
	UserAvatar   sql.NullString
	UserState    UserInfoUserState
	UserMobile   sql.NullString
	UserGender   NullUserInfoUserGender
	UserBirthday sql.NullTime
	UserEmail    sql.NullString
	CreatedAt    sql.NullTime
	UpdatedAt    sql.NullTime
}

func (q *Queries) GetUsers(ctx context.Context, userID string) ([]GetUsersRow, error) {
	rows, err := q.db.QueryContext(ctx, getUsers, userID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var items []GetUsersRow
	for rows.Next() {
		var i GetUsersRow
		if err := rows.Scan(
			&i.UserID,
			&i.UserAccount,
			&i.UserNickname,
			&i.UserAvatar,
			&i.UserState,
			&i.UserMobile,
			&i.UserGender,
			&i.UserBirthday,
			&i.UserEmail,
			&i.CreatedAt,
			&i.UpdatedAt,
		); err != nil {
			return nil, err
		}
		items = append(items, i)
	}
	if err := rows.Close(); err != nil {
		return nil, err
	}
	if err := rows.Err(); err != nil {
		return nil, err
	}
	return items, nil
}

const listUsers = `-- name: ListUsers :many
SELECT user_id, user_account, user_nickname, user_avatar, user_state, user_mobile, user_gender, user_birthday, user_email, user_is_authentication, created_at, updated_at FROM user_info LIMIT ? OFFSET ?
`

type ListUsersParams struct {
	Limit  int32
	Offset int32
}

func (q *Queries) ListUsers(ctx context.Context, arg ListUsersParams) ([]UserInfo, error) {
	rows, err := q.db.QueryContext(ctx, listUsers, arg.Limit, arg.Offset)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var items []UserInfo
	for rows.Next() {
		var i UserInfo
		if err := rows.Scan(
			&i.UserID,
			&i.UserAccount,
			&i.UserNickname,
			&i.UserAvatar,
			&i.UserState,
			&i.UserMobile,
			&i.UserGender,
			&i.UserBirthday,
			&i.UserEmail,
			&i.UserIsAuthentication,
			&i.CreatedAt,
			&i.UpdatedAt,
		); err != nil {
			return nil, err
		}
		items = append(items, i)
	}
	if err := rows.Close(); err != nil {
		return nil, err
	}
	if err := rows.Err(); err != nil {
		return nil, err
	}
	return items, nil
}

const removeUser = `-- name: RemoveUser :exec
DELETE FROM user_info WHERE user_id = ?
`

func (q *Queries) RemoveUser(ctx context.Context, userID string) error {
	_, err := q.db.ExecContext(ctx, removeUser, userID)
	return err
}
