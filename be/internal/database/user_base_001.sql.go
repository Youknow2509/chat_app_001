// Code generated by sqlc. DO NOT EDIT.
// versions:
//   sqlc v1.27.0
// source: user_base_001.sql

package database

import (
	"context"
	"database/sql"
)

const addUserBase = `-- name: AddUserBase :execresult
INSERT INTO ` + "`" + `user_base_001` + "`" + ` (
    user_account, user_password, user_salt, user_created_at, user_updated_at
) VALUES (
    ?, ?, ?, NOW(), NOW()
)
`

type AddUserBaseParams struct {
	UserAccount  string
	UserPassword string
	UserSalt     string
}

func (q *Queries) AddUserBase(ctx context.Context, arg AddUserBaseParams) (sql.Result, error) {
	return q.db.ExecContext(ctx, addUserBase, arg.UserAccount, arg.UserPassword, arg.UserSalt)
}

const checkUserBaseExists = `-- name: CheckUserBaseExists :one
SELECT COUNT(*)
FROM ` + "`" + `user_base_001` + "`" + `
WHERE user_account = ?
`

func (q *Queries) CheckUserBaseExists(ctx context.Context, userAccount string) (int64, error) {
	row := q.db.QueryRowContext(ctx, checkUserBaseExists, userAccount)
	var count int64
	err := row.Scan(&count)
	return count, err
}

const getOneUserInfo = `-- name: GetOneUserInfo :one
SELECT user_id, user_account, user_password, user_salt
FROM ` + "`" + `user_base_001` + "`" + `
WHERE user_account = ?
`

type GetOneUserInfoRow struct {
	UserID       int32
	UserAccount  string
	UserPassword string
	UserSalt     string
}

func (q *Queries) GetOneUserInfo(ctx context.Context, userAccount string) (GetOneUserInfoRow, error) {
	row := q.db.QueryRowContext(ctx, getOneUserInfo, userAccount)
	var i GetOneUserInfoRow
	err := row.Scan(
		&i.UserID,
		&i.UserAccount,
		&i.UserPassword,
		&i.UserSalt,
	)
	return i, err
}

const getOneUserInfoAdmin = `-- name: GetOneUserInfoAdmin :one
SELECT user_id, user_account, user_password, user_salt, user_login_time, user_logout_time, user_login_ip
    , user_created_at, user_updated_at
FROM ` + "`" + `user_base_001` + "`" + `
WHERE user_account = ?
`

type GetOneUserInfoAdminRow struct {
	UserID         int32
	UserAccount    string
	UserPassword   string
	UserSalt       string
	UserLoginTime  sql.NullTime
	UserLogoutTime sql.NullTime
	UserLoginIp    sql.NullString
	UserCreatedAt  sql.NullTime
	UserUpdatedAt  sql.NullTime
}

func (q *Queries) GetOneUserInfoAdmin(ctx context.Context, userAccount string) (GetOneUserInfoAdminRow, error) {
	row := q.db.QueryRowContext(ctx, getOneUserInfoAdmin, userAccount)
	var i GetOneUserInfoAdminRow
	err := row.Scan(
		&i.UserID,
		&i.UserAccount,
		&i.UserPassword,
		&i.UserSalt,
		&i.UserLoginTime,
		&i.UserLogoutTime,
		&i.UserLoginIp,
		&i.UserCreatedAt,
		&i.UserUpdatedAt,
	)
	return i, err
}

const loginUserBase = `-- name: LoginUserBase :exec
UPDATE ` + "`" + `user_base_001` + "`" + `
SET user_login_time = NOW(), user_login_ip = ?
WHERE user_account = ? AND user_password = ?
`

type LoginUserBaseParams struct {
	UserLoginIp  sql.NullString
	UserAccount  string
	UserPassword string
}

func (q *Queries) LoginUserBase(ctx context.Context, arg LoginUserBaseParams) error {
	_, err := q.db.ExecContext(ctx, loginUserBase, arg.UserLoginIp, arg.UserAccount, arg.UserPassword)
	return err
}

const logoutUserBase = `-- name: LogoutUserBase :exec
UPDATE ` + "`" + `user_base_001` + "`" + `
SET user_logout_time = NOW()
WHERE user_account = ?
`

func (q *Queries) LogoutUserBase(ctx context.Context, userAccount string) error {
	_, err := q.db.ExecContext(ctx, logoutUserBase, userAccount)
	return err
}

const updatePassword = `-- name: UpdatePassword :exec
UPDATE ` + "`" + `user_base_001` + "`" + ` 
SET user_password = ? WHERE user_id = ?
`

type UpdatePasswordParams struct {
	UserPassword string
	UserID       int32
}

func (q *Queries) UpdatePassword(ctx context.Context, arg UpdatePasswordParams) error {
	_, err := q.db.ExecContext(ctx, updatePassword, arg.UserPassword, arg.UserID)
	return err
}
