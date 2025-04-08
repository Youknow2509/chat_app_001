
-- name: GetFriendRequestInfoWithUser :one
SELECT id, from_user, to_user, status, created_at
FROM friend_requests
WHERE (from_user = ? AND to_user = ?) OR (from_user = ? AND to_user = ?)
LIMIT 1;

-- name: CheckFriendRequestExists :one
SELECT COUNT(*)
FROM friend_requests
WHERE (from_user = ? AND to_user = ?) OR (from_user = ? AND to_user = ?);

-- name: GetFriendID :one
SELECT user_id
FROM friends
WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)
LIMIT 1;

-- name: CheckFriendRequestExistsWithID :one
SELECT COUNT(*) 
FROM friend_requests
WHERE id = ?;

-- name: GetFriendRequestInfo :one
SELECT * 
FROM friend_requests
WHERE id = ?
LIMIT 1;

-- name: GetFriendUser :many
SELECT u.user_id, u.user_nickname, u.user_email, u.user_avatar
FROM user_info u
JOIN friends f ON u.user_id = f.friend_id OR u.user_id = f.user_id
WHERE (f.user_id = ? OR f.friend_id = ?) 
AND u.user_id <> ?
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
SET status = 'accepted', updated_at = now()
WHERE id = ?;

-- name: DeclineFriendRequest :exec
UPDATE friend_requests
SET status = 'declined', updated_at = now()
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
    fr.id,
    fr.to_user,
    ui.user_nickname,
    ui.user_avatar,
    fr.status,
    fr.created_at
FROM friend_requests fr
JOIN user_info ui ON ui.user_id = fr.to_user
WHERE fr.from_user = ? AND fr.status = 'pending'
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