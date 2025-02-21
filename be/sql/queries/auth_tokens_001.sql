-- name: GetAccessToken :one
SELECT 
    id,
    access_token,
    user_id,
    expires_at,
    created_at,
    expires_at
FROM auth_tokens WHERE access_token = ? LIMIT 1;

-- name: GetAccessTokenByUserID :many
SELECT 
    id,
    access_token,
    user_id,
    expires_at,
    created_at,
    expires_at
FROM auth_tokens WHERE user_id = ?;

-- name: GetAccessTokenByCacheKey :one
SELECT 
    id,
    access_token,
    user_id,
    expires_at,
    created_at,
    expires_at
FROM auth_tokens WHERE cache_key = ? LIMIT 1;

-- name: InsertAccessToken :exec
INSERT INTO auth_tokens (
    id, 
    user_id, 
    cache_key,
    access_token, 
    created_at, 
    expires_at
)
VALUES (?, ?, ?, ?, now(), ?);

-- name: DeleteAccessToken :exec
DELETE FROM auth_tokens WHERE access_token = ?;

-- name: DeleteAccessTokenByID :exec
DELETE FROM auth_tokens WHERE id = ?;

-- name: DeleteAccessTokenByUserID :exec
DELETE FROM auth_tokens WHERE user_id = ?;

-- name: DeleteExpiredAccessTokens :exec
DELETE FROM auth_tokens WHERE expires_at < now();

-- name: DeleteExpiredAccessTokensByUserID :exec
DELETE FROM auth_tokens WHERE expires_at < now() AND user_id = ?;
