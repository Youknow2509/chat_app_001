-- name: GetOneUserInfo :one
SELECT user_id, user_account, user_password, user_salt, user_is_refresh_token
FROM `user_base`
WHERE user_account = ?;

-- name: GetOneUserInfoAdmin :one
SELECT user_id, user_account, user_password, user_salt, user_is_refresh_token,
    user_login_time, user_logout_time, user_login_ip, user_created_at, user_updated_at
FROM `user_base`
WHERE user_account = ?;

-- name: GetIDUserWithEmail :one
SELECT user_id
FROM `user_base`
WHERE user_account = ?
LIMIT 1;

-- name: CheckUserBaseExists :one
SELECT COUNT(*)
FROM `user_base`
WHERE user_account = ?;

-- name: CheckUserBaseExistsWithID :one
SELECT COUNT(*)
FROM `user_base`
WHERE user_id = ?;

-- name: AddUserBaseWithUUID :execresult
INSERT INTO `user_base` (
    user_id,
    user_account, user_password, user_salt, user_is_refresh_token, 
    user_created_at, user_updated_at
) VALUES (
    ?, ?, ?, ?, 0, NOW(), NOW()
);

-- name: UpdatePasswordWithUserID :exec
UPDATE `user_base` 
SET user_password = ? WHERE user_id = ?;

-- name: LoginUserBase :exec
UPDATE `user_base`
SET user_login_time = NOW(), user_login_ip = ?, user_is_refresh_token = 0
WHERE user_account = ? AND user_password = ?;

-- name: LogoutUserBase :exec
UPDATE `user_base`
SET user_logout_time = NOW()
WHERE user_account = ?;

-- name: RefreshTokenUserOn :exec
UPDATE `user_base`
SET user_is_refresh_token = 0
WHERE user_account = ?;

-- name: RefreshTokenUserOff :exec
UPDATE `user_base`
SET user_is_refresh_token = 1
WHERE user_account = ?;

-- name: IsRefreshTokenUser :one
SELECT user_is_refresh_token
FROM `user_base`
WHERE user_account = ?;

-- name: IsRefreshTokenUserWithID :one
SELECT user_is_refresh_token
FROM `user_base`
WHERE user_id = ?;
