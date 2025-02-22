-- name: GetUserWithID :one
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
FROM `user_info` INNER JOIN `user_base`
    ON user_base.user_id = user_info.user_id
WHERE user_info.user_id = ? LIMIT 1;

-- name: GetUserWithAccount :one
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
FROM `user_info`
WHERE user_account = ? LIMIT 1;

-- name: GetUsers :many
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
FROM `user_info`
WHERE user_id IN (?);

-- name: FindUsers :many
SELECT * FROM user_info 
WHERE user_account LIKE ? OR user_nickname LIKE ?
ORDER BY user_nickname ASC
LIMIT ? OFFSET ?;

-- name: ListUsers :many
SELECT * FROM user_info LIMIT ? OFFSET ?;

-- name: RemoveUser :exec
DELETE FROM user_info WHERE user_id = ?;

-- name: AddUserHaveUserId :execresult
INSERT INTO `user_info` (
    user_id, user_account, user_nickname, 
    user_avatar, user_state, user_mobile, 
    user_gender, user_birthday, user_email)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);

-- name: EditUserByUserId :execresult
UPDATE `user_info`
SET user_nickname = ?, user_avatar = ?, user_mobile = ?,
    user_gender = ?, user_birthday = ?, user_email = ?, 
    updated_at = NOW()
WHERE user_id = ?;

-- name: FindUserWithMail :many
SELECT * FROM user_info WHERE user_email LIKE ?
ORDER BY user_nickname ASC
LIMIT ? OFFSET ?;