

-- name: GetFriendUser :many
SELECT ui.user_id, ui.user_nickname, ui.user_avatar, ui.user_email
FROM (
    SELECT 
        CASE 
            WHEN friends.user_id = ? THEN friend_id 
            ELSE friends.user_id 
        END AS friend_id
    FROM friends
    WHERE friends.user_id = ? OR friend_id = ?
) AS f
JOIN user_info ui ON ui.user_id = f.friend_id
ORDER BY ui.user_nickname ASC
LIMIT ? OFFSET ?;

-- name: InsertFriendRequest :exec
INSERT INTO friend_requests (id, from_user, to_user, status, created_at)
VALUES (?, ?, ?, 'pending', now());

-- name: UpdateFriendRequest :exec
UPDATE friend_requests
SET status = ?
WHERE id = ?;

-- name: ResponseFriendRequest :exec
UPDATE friend_requests
SET status = ?
WHERE id = ?;

-- name: GetFriendRequestUserSend :many
SELECT
    id,
    to_user,
    status,
    created_at
FROM friend_requests 
WHERE from_user = ?
ORDER BY created_at DESC
LIMIT ? OFFSET ?;

-- name: GetFriendRequestUserReceive :many
SELECT
    id,
    from_user,
    status,
    created_at
FROM friend_requests
WHERE to_user = ?
ORDER BY created_at DESC
LIMIT ? OFFSET ?;

-- name: GetFriendRequestCount :one
SELECT COUNT(*) AS count
FROM friend_requests
WHERE (from_user = ? AND status = 'pending') OR (to_user = ? AND status = 'pending');

-- name: DeleteFriendRequest :exec  
DELETE FROM friend_requests
WHERE id = ?;