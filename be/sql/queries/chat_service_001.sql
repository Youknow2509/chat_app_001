-- name: CreateChat :exec
INSERT INTO chats (id, group_name, type, created_at, updated_at)
VALUES (?, ?, 'private', now(), now());

-- name: CreateGroupChat :exec
INSERT INTO chats (id, type, group_name, created_at, updated_at)
VALUES (?, 'group', ?, now(), now());

-- name: CheckPrivateChatExists :one
SELECT c.id
FROM chats c
JOIN chat_members cm1 ON c.id = cm1.chat_id
JOIN chat_members cm2 ON c.id = cm2.chat_id
WHERE c.type = 'private'
  AND cm1.user_id = ?
  AND cm2.user_id = ?
LIMIT 1;

-- name: CheckUserInChat :one
SELECT COUNT(*) 
FROM chats ch JOIN chat_members ch_m
    ON ch.id = ch_m.chat_id
WHERE ch.id = ? AND ch_m.user_id = ?;

-- name: InsertChatMember :exec
INSERT INTO chat_members (chat_id, user_id, role)
VALUES (?, ?, ?);

-- name: GetGroupInfo :one
SELECT 
    c.id AS groupId,
    c.group_name,
    COUNT(cm.user_id) AS numberOfMember,
    GROUP_CONCAT(cm.user_id) AS list_mem,
    c.type AS chat_type,
    c.group_avatar AS chat_avatar
FROM chats AS c
JOIN chat_members AS cm ON c.id = cm.chat_id
WHERE c.id = ?
GROUP BY c.id;

-- name: GetChatList :many
SELECT id AS groupId, group_name AS groupName, updated_at
FROM chats
ORDER BY updated_at DESC
LIMIT ? OFFSET ?;

-- name: AddMemberToChat :exec
INSERT INTO chat_members (chat_id, user_id, role)
VALUES (?, ?, 'member');

-- name: UpdateGroupChat :exec
UPDATE chats
SET group_name = ?, group_avatar = ?, updated_at = now()
WHERE id = ?;

-- name: ChangeGroupAdmin :exec
UPDATE chat_members
SET role = 'admin'
WHERE chat_id = ? AND user_id = ?;

-- name: ChangeToMember :exec
UPDATE chat_members
SET role ='member'
WHERE chat_id = ? AND user_id = ?;

-- name: DeleteMemberFromChat :exec
DELETE FROM chat_members
WHERE chat_id = ? AND user_id = ?;

-- name: DeleteChat :exec
DELETE FROM chats
WHERE id = ?;

-- name: CheckAdminGroupChat :one
SELECT COUNT(*)
FROM chat_members
WHERE chat_id = ? AND user_id = ? AND role = 'admin';

-- name: CheckUserInGroupChat :one
SELECT COUNT(*)
FROM chat_members
WHERE chat_id = ? AND user_id = ? AND role = 'member';

-- name: GetChatListForUser :many
SELECT
    c.id AS chat_id,
    c.group_name AS chat_name,
    c.group_avatar AS chat_avatar,
    c.updated_at AS chat_updated_at,
    c.type AS chat_type
FROM chats c
JOIN chat_members cm 
    ON c.id = cm.chat_id
WHERE cm.user_id = ?
ORDER BY c.updated_at DESC
LIMIT ? OFFSET ?;

-- name: GetChatListPrivateForUser :many
SELECT 
    c.id AS id_chat,
    u.user_avatar AS partner_avatar,
    u.user_nickname AS partner_name
FROM 
    chats c
    INNER JOIN chat_members cm ON c.id = cm.chat_id 
    INNER JOIN chat_members other_cm ON c.id = other_cm.chat_id AND other_cm.user_id != ?
    INNER JOIN user_info u ON other_cm.user_id = u.user_id
WHERE 
    c.type = 'private'
    AND cm.user_id = ?
ORDER BY 
    c.updated_at DESC
LIMIT ?, ?;

-- name: GetChatListGroupForUser :many
SELECT
    c.id AS chat_id,
    c.group_name AS chat_name,
    c.group_avatar AS chat_avatar,
    c.updated_at AS chat_updated_at,
    c.type AS chat_type
FROM chats c
JOIN chat_members cm 
    ON c.id = cm.chat_id
WHERE cm.user_id = ? AND c.type = 'group'
ORDER BY c.updated_at DESC
LIMIT ? OFFSET ?;

-- name: GetAllUsersInChat :many
SELECT 
    ui.user_id,
    ui.user_nickname,
    ui.user_avatar,
    ui.user_email
FROM chat_members cm
JOIN user_info ui ON cm.user_id = ui.user_id
WHERE cm.chat_id = ?
ORDER BY ui.user_nickname ASC;

-- name: GetUsersInChat :many
SELECT 
    ui.user_id,
    ui.user_nickname,
    ui.user_avatar,
    ui.user_email
FROM chat_members cm
JOIN user_info ui ON cm.user_id = ui.user_id
WHERE cm.chat_id = ?
ORDER BY ui.user_nickname ASC
LIMIT ? OFFSET ?;



    