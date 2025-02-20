-- name: GetRefreshToken :one
SELECT 
    id,
    refresh_token,
    user_id,
    expires_at,
    created_at
FROM refresh_tokens WHERE refresh_token = ?;

-- name: GetRefreshTokenByUserID :many
SELECT 
    id,
    refresh_token,
    user_id,
    expires_at,
    created_at
FROM refresh_tokens WHERE user_id = ?;

-- name: GetRefreshTokenByID :one
SELECT 
    id,
    refresh_token,
    user_id,
    expires_at,
    created_at
FROM refresh_tokens WHERE id = ?;

-- name: InsertRefreshToken :exec
INSERT INTO refresh_tokens (
    id,
    refresh_token,
    user_id,
    expires_at,
    created_at
) VALUES (?, ?, ?, ?, now());

-- name: DeleteRefreshToken :exec
DELETE FROM refresh_tokens WHERE id = ?;

-- name: DeleteRefreshTokenByUserID :exec
DELETE FROM refresh_tokens WHERE user_id = ?;

-- name: DeleteExpiredRefreshTokens :exec
DELETE FROM refresh_tokens WHERE expires_at < now();

-- name: DeleteExpiredRefreshTokensByUserID :exec
DELETE FROM refresh_tokens WHERE expires_at < now() AND user_id = ?;


