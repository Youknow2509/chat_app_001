// Code generated by sqlc. DO NOT EDIT.
// versions:
//   sqlc v1.27.0
// source: auth_tokens_001.sql

package database

import (
	"context"
	"database/sql"
	"time"
)

const deleteAccessToken = `-- name: DeleteAccessToken :exec
DELETE FROM auth_tokens WHERE access_token = ?
`

func (q *Queries) DeleteAccessToken(ctx context.Context, accessToken string) error {
	_, err := q.db.ExecContext(ctx, deleteAccessToken, accessToken)
	return err
}

const deleteAccessTokenByID = `-- name: DeleteAccessTokenByID :exec
DELETE FROM auth_tokens WHERE id = ?
`

func (q *Queries) DeleteAccessTokenByID(ctx context.Context, id string) error {
	_, err := q.db.ExecContext(ctx, deleteAccessTokenByID, id)
	return err
}

const deleteAccessTokenByUserID = `-- name: DeleteAccessTokenByUserID :exec
DELETE FROM auth_tokens WHERE user_id = ?
`

func (q *Queries) DeleteAccessTokenByUserID(ctx context.Context, userID string) error {
	_, err := q.db.ExecContext(ctx, deleteAccessTokenByUserID, userID)
	return err
}

const deleteExpiredAccessTokens = `-- name: DeleteExpiredAccessTokens :exec
DELETE FROM auth_tokens WHERE expires_at < now()
`

func (q *Queries) DeleteExpiredAccessTokens(ctx context.Context) error {
	_, err := q.db.ExecContext(ctx, deleteExpiredAccessTokens)
	return err
}

const deleteExpiredAccessTokensByUserID = `-- name: DeleteExpiredAccessTokensByUserID :exec
DELETE FROM auth_tokens WHERE expires_at < now() AND user_id = ?
`

func (q *Queries) DeleteExpiredAccessTokensByUserID(ctx context.Context, userID string) error {
	_, err := q.db.ExecContext(ctx, deleteExpiredAccessTokensByUserID, userID)
	return err
}

const getAccessToken = `-- name: GetAccessToken :one
SELECT 
    id,
    access_token,
    user_id,
    expires_at,
    created_at,
    expires_at
FROM auth_tokens WHERE access_token = ? LIMIT 1
`

type GetAccessTokenRow struct {
	ID          string
	AccessToken string
	UserID      string
	ExpiresAt   time.Time
	CreatedAt   sql.NullTime
	ExpiresAt_2 time.Time
}

func (q *Queries) GetAccessToken(ctx context.Context, accessToken string) (GetAccessTokenRow, error) {
	row := q.db.QueryRowContext(ctx, getAccessToken, accessToken)
	var i GetAccessTokenRow
	err := row.Scan(
		&i.ID,
		&i.AccessToken,
		&i.UserID,
		&i.ExpiresAt,
		&i.CreatedAt,
		&i.ExpiresAt_2,
	)
	return i, err
}

const getAccessTokenByCacheKey = `-- name: GetAccessTokenByCacheKey :one
SELECT 
    id,
    access_token,
    user_id,
    expires_at,
    created_at,
    expires_at
FROM auth_tokens WHERE cache_key = ? LIMIT 1
`

type GetAccessTokenByCacheKeyRow struct {
	ID          string
	AccessToken string
	UserID      string
	ExpiresAt   time.Time
	CreatedAt   sql.NullTime
	ExpiresAt_2 time.Time
}

func (q *Queries) GetAccessTokenByCacheKey(ctx context.Context, cacheKey string) (GetAccessTokenByCacheKeyRow, error) {
	row := q.db.QueryRowContext(ctx, getAccessTokenByCacheKey, cacheKey)
	var i GetAccessTokenByCacheKeyRow
	err := row.Scan(
		&i.ID,
		&i.AccessToken,
		&i.UserID,
		&i.ExpiresAt,
		&i.CreatedAt,
		&i.ExpiresAt_2,
	)
	return i, err
}

const getAccessTokenByUserID = `-- name: GetAccessTokenByUserID :many
SELECT 
    id,
    access_token,
    user_id,
    expires_at,
    created_at,
    expires_at
FROM auth_tokens WHERE user_id = ?
`

type GetAccessTokenByUserIDRow struct {
	ID          string
	AccessToken string
	UserID      string
	ExpiresAt   time.Time
	CreatedAt   sql.NullTime
	ExpiresAt_2 time.Time
}

func (q *Queries) GetAccessTokenByUserID(ctx context.Context, userID string) ([]GetAccessTokenByUserIDRow, error) {
	rows, err := q.db.QueryContext(ctx, getAccessTokenByUserID, userID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var items []GetAccessTokenByUserIDRow
	for rows.Next() {
		var i GetAccessTokenByUserIDRow
		if err := rows.Scan(
			&i.ID,
			&i.AccessToken,
			&i.UserID,
			&i.ExpiresAt,
			&i.CreatedAt,
			&i.ExpiresAt_2,
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

const getMailUserWithAccessToken = `-- name: GetMailUserWithAccessToken :one
SELECT 
    ub.user_id,
    ub.user_account
FROM auth_tokens at JOIN user_base ub 
    ON at.user_id = ub.user_id
WHERE at.access_token = ?
LIMIT 1
`

type GetMailUserWithAccessTokenRow struct {
	UserID      string
	UserAccount string
}

func (q *Queries) GetMailUserWithAccessToken(ctx context.Context, accessToken string) (GetMailUserWithAccessTokenRow, error) {
	row := q.db.QueryRowContext(ctx, getMailUserWithAccessToken, accessToken)
	var i GetMailUserWithAccessTokenRow
	err := row.Scan(&i.UserID, &i.UserAccount)
	return i, err
}

const getUserIDWithAccessToken = `-- name: GetUserIDWithAccessToken :one
SELECT 
    id,
    user_id
FROM auth_tokens WHERE access_token = ? LIMIT 1
`

type GetUserIDWithAccessTokenRow struct {
	ID     string
	UserID string
}

func (q *Queries) GetUserIDWithAccessToken(ctx context.Context, accessToken string) (GetUserIDWithAccessTokenRow, error) {
	row := q.db.QueryRowContext(ctx, getUserIDWithAccessToken, accessToken)
	var i GetUserIDWithAccessTokenRow
	err := row.Scan(&i.ID, &i.UserID)
	return i, err
}

const getValidAccessTokensWithUserID = `-- name: GetValidAccessTokensWithUserID :many
SELECT 
    id,
    cache_key
FROM auth_tokens
WHERE expires_at > CURRENT_TIMESTAMP AND user_id = ?
`

type GetValidAccessTokensWithUserIDRow struct {
	ID       string
	CacheKey string
}

func (q *Queries) GetValidAccessTokensWithUserID(ctx context.Context, userID string) ([]GetValidAccessTokensWithUserIDRow, error) {
	rows, err := q.db.QueryContext(ctx, getValidAccessTokensWithUserID, userID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var items []GetValidAccessTokensWithUserIDRow
	for rows.Next() {
		var i GetValidAccessTokensWithUserIDRow
		if err := rows.Scan(&i.ID, &i.CacheKey); err != nil {
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

const insertAccessToken = `-- name: InsertAccessToken :exec
INSERT INTO auth_tokens (
    id, 
    user_id, 
    cache_key,
    access_token, 
    created_at, 
    expires_at
)
VALUES (?, ?, ?, ?, now(), ?)
`

type InsertAccessTokenParams struct {
	ID          string
	UserID      string
	CacheKey    string
	AccessToken string
	ExpiresAt   time.Time
}

func (q *Queries) InsertAccessToken(ctx context.Context, arg InsertAccessTokenParams) error {
	_, err := q.db.ExecContext(ctx, insertAccessToken,
		arg.ID,
		arg.UserID,
		arg.CacheKey,
		arg.AccessToken,
		arg.ExpiresAt,
	)
	return err
}
