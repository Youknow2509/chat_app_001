
-- name: GetValidRefreshTokensByUserID :many
SELECT 
    id
FROM refresh_tokens
WHERE expires_at > CURRENT_TIMESTAMP 
    AND is_used = 1
    AND user_id = ?;

-- name: ExecTokenUsed :exec
UPDATE refresh_tokens
SET is_used = 0
WHERE refresh_token = ?;

-- name: ExecTokenUsedWithID :exec
UPDATE refresh_tokens
SET is_used = 0
WHERE id = ?;

-- name: GetStatusRefreshToken :one
SELECT is_used
FROM refresh_tokens 
WHERE refresh_token = ?
LIMIT 1;

-- name: GetStatusRefreshTokenWithID :one
SELECT is_used
FROM refresh_tokens 
WHERE id = ?
LIMIT 1;

-- name: GetRefreshToken :one
SELECT 
    id,
    refresh_token,
    user_id,
    is_used,
    expires_at,
    created_at,
    updated_at
FROM refresh_tokens WHERE refresh_token = ?;

-- name: GetRefreshTokenByUserID :many
SELECT *
FROM refresh_tokens WHERE user_id = ?;

-- name: GetRefreshTokenByID :one
SELECT *
FROM refresh_tokens WHERE id = ?;

-- name: InsertRefreshToken :exec
INSERT INTO refresh_tokens (
    id,
    refresh_token,
    user_id,
    expires_at,
    created_at,
    updated_at
) VALUES (?, ?, ?, ?, now(), now());

-- name: DeleteRefreshToken :exec
DELETE FROM refresh_tokens WHERE id = ?;

-- name: DeleteRefreshTokenByUserID :exec
DELETE FROM refresh_tokens WHERE user_id = ?;

-- name: DeleteExpiredRefreshTokens :exec
DELETE FROM refresh_tokens WHERE expires_at < now();

-- name: DeleteExpiredRefreshTokensByUserID :exec
DELETE FROM refresh_tokens WHERE expires_at < now() AND user_id = ?;