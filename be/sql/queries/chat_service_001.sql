-- name: CreateChat :exec
INSERT INTO chats (id, type, created_at, updated_at)
VALUES (?, 'private', now(), now());

-- name: CreateGroupChat :exec
INSERT INTO chats (id, type, group_name, created_at, updated_at)
VALUES (?, 'group', ?, now(), now());

-- name: InsertChatMember :exec
INSERT INTO chat_members (chat_id, user_id, role)
VALUES (?, ?, ?);

-- name: GetGroupInfo :one
SELECT c.group_name,
       COUNT(cm.user_id) AS numberOfMember,
       GROUP_CONCAT(cm.user_id) AS list_mem
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
