
-- name: GetFriendID :one
SELECT user_id
FROM friends
WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)
LIMIT 1;

-- name: CheckFriendRequestExists :one
SELECT COUNT(*) 
FROM friend_requests
WHERE id = ?;

-- name: GetFriendRequestInfo :one
SELECT * 
FROM friend_requests
WHERE id = ?
LIMIT 1;

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
INSERT INTO friend_requests (id, from_user, to_user, status, created_at, updated_at)
VALUES (?, ?, ?, 'pending', now(), now());

-- name: UpdateFriendRequest :exec
UPDATE friend_requests
SET status = ?
WHERE id = ?;

-- name: AcceptFriendRequest :exec
UPDATE friend_requests
SET status = "accepted" AND updated_at = now()
WHERE id = ?;

-- name: DeclineFriendRequest :exec
UPDATE friend_requests
SET status = "declined" AND updated_at = now()
WHERE id = ?;

-- name: AddFriend :exec
INSERT INTO friends (user_id, friend_id)
VALUES (?, ?);

-- name: DeleteFriend :exec
DELETE FROM friends
WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?);

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