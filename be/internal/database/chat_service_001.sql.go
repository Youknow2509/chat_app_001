// Code generated by sqlc. DO NOT EDIT.
// versions:
//   sqlc v1.27.0
// source: chat_service_001.sql

package database

import (
	"context"
	"database/sql"
)

const addMemberToChat = `-- name: AddMemberToChat :exec
INSERT INTO chat_members (chat_id, user_id, role)
VALUES (?, ?, 'member')
`

type AddMemberToChatParams struct {
	ChatID string
	UserID string
}

func (q *Queries) AddMemberToChat(ctx context.Context, arg AddMemberToChatParams) error {
	_, err := q.db.ExecContext(ctx, addMemberToChat, arg.ChatID, arg.UserID)
	return err
}

const changeGroupAdmin = `-- name: ChangeGroupAdmin :exec
UPDATE chat_members
SET role = 'admin'
WHERE chat_id = ? AND user_id = ?
`

type ChangeGroupAdminParams struct {
	ChatID string
	UserID string
}

func (q *Queries) ChangeGroupAdmin(ctx context.Context, arg ChangeGroupAdminParams) error {
	_, err := q.db.ExecContext(ctx, changeGroupAdmin, arg.ChatID, arg.UserID)
	return err
}

const checkAdminGroupChat = `-- name: CheckAdminGroupChat :one
SELECT COUNT(*)
FROM chat_members
WHERE chat_id = ? AND user_id = ? AND role = 'admin'
`

type CheckAdminGroupChatParams struct {
	ChatID string
	UserID string
}

func (q *Queries) CheckAdminGroupChat(ctx context.Context, arg CheckAdminGroupChatParams) (int64, error) {
	row := q.db.QueryRowContext(ctx, checkAdminGroupChat, arg.ChatID, arg.UserID)
	var count int64
	err := row.Scan(&count)
	return count, err
}

const checkPrivateChatExists = `-- name: CheckPrivateChatExists :one
SELECT c.id
FROM chats c
JOIN chat_members cm1 ON c.id = cm1.chat_id
JOIN chat_members cm2 ON c.id = cm2.chat_id
WHERE c.type = 'private'
  AND cm1.user_id = ?
  AND cm2.user_id = ?
LIMIT 1
`

type CheckPrivateChatExistsParams struct {
	UserID   string
	UserID_2 string
}

func (q *Queries) CheckPrivateChatExists(ctx context.Context, arg CheckPrivateChatExistsParams) (string, error) {
	row := q.db.QueryRowContext(ctx, checkPrivateChatExists, arg.UserID, arg.UserID_2)
	var id string
	err := row.Scan(&id)
	return id, err
}

const checkUserInChat = `-- name: CheckUserInChat :one
SELECT COUNT(*) 
FROM chats ch JOIN chat_members ch_m
    ON ch.id = ch_m.chat_id
WHERE ch.id = ? AND ch_m.user_id = ?
`

type CheckUserInChatParams struct {
	ID     string
	UserID string
}

func (q *Queries) CheckUserInChat(ctx context.Context, arg CheckUserInChatParams) (int64, error) {
	row := q.db.QueryRowContext(ctx, checkUserInChat, arg.ID, arg.UserID)
	var count int64
	err := row.Scan(&count)
	return count, err
}

const createChat = `-- name: CreateChat :exec
INSERT INTO chats (id, group_name, type, created_at, updated_at)
VALUES (?, ?, 'private', now(), now())
`

type CreateChatParams struct {
	ID        string
	GroupName sql.NullString
}

func (q *Queries) CreateChat(ctx context.Context, arg CreateChatParams) error {
	_, err := q.db.ExecContext(ctx, createChat, arg.ID, arg.GroupName)
	return err
}

const createGroupChat = `-- name: CreateGroupChat :exec
INSERT INTO chats (id, type, group_name, created_at, updated_at)
VALUES (?, 'group', ?, now(), now())
`

type CreateGroupChatParams struct {
	ID        string
	GroupName sql.NullString
}

func (q *Queries) CreateGroupChat(ctx context.Context, arg CreateGroupChatParams) error {
	_, err := q.db.ExecContext(ctx, createGroupChat, arg.ID, arg.GroupName)
	return err
}

const deleteChat = `-- name: DeleteChat :exec
DELETE FROM chats
WHERE id = ?
`

func (q *Queries) DeleteChat(ctx context.Context, id string) error {
	_, err := q.db.ExecContext(ctx, deleteChat, id)
	return err
}

const deleteMemberFromChat = `-- name: DeleteMemberFromChat :exec
DELETE FROM chat_members
WHERE chat_id = ? AND user_id = ?
`

type DeleteMemberFromChatParams struct {
	ChatID string
	UserID string
}

func (q *Queries) DeleteMemberFromChat(ctx context.Context, arg DeleteMemberFromChatParams) error {
	_, err := q.db.ExecContext(ctx, deleteMemberFromChat, arg.ChatID, arg.UserID)
	return err
}

const getAllUsersInChat = `-- name: GetAllUsersInChat :many
SELECT 
    ui.user_id,
    ui.user_nickname,
    ui.user_avatar,
    ui.user_email
FROM chat_members cm
JOIN user_info ui ON cm.user_id = ui.user_id
WHERE cm.chat_id = ?
ORDER BY ui.user_nickname ASC
`

type GetAllUsersInChatRow struct {
	UserID       string
	UserNickname sql.NullString
	UserAvatar   sql.NullString
	UserEmail    sql.NullString
}

func (q *Queries) GetAllUsersInChat(ctx context.Context, chatID string) ([]GetAllUsersInChatRow, error) {
	rows, err := q.db.QueryContext(ctx, getAllUsersInChat, chatID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var items []GetAllUsersInChatRow
	for rows.Next() {
		var i GetAllUsersInChatRow
		if err := rows.Scan(
			&i.UserID,
			&i.UserNickname,
			&i.UserAvatar,
			&i.UserEmail,
		); err != nil {
			return nil, err
		}
		items = append(items, i)
	}
	if err := rows.Close(); err != nil {
		return nil, err
	}
	if err := rows.Err(); err != nil {
		return nil, err
	}
	return items, nil
}

const getChatList = `-- name: GetChatList :many
SELECT id AS groupId, group_name AS groupName, updated_at
FROM chats
ORDER BY updated_at DESC
LIMIT ? OFFSET ?
`

type GetChatListParams struct {
	Limit  int32
	Offset int32
}

type GetChatListRow struct {
	Groupid   string
	Groupname sql.NullString
	UpdatedAt sql.NullTime
}

func (q *Queries) GetChatList(ctx context.Context, arg GetChatListParams) ([]GetChatListRow, error) {
	rows, err := q.db.QueryContext(ctx, getChatList, arg.Limit, arg.Offset)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var items []GetChatListRow
	for rows.Next() {
		var i GetChatListRow
		if err := rows.Scan(&i.Groupid, &i.Groupname, &i.UpdatedAt); err != nil {
			return nil, err
		}
		items = append(items, i)
	}
	if err := rows.Close(); err != nil {
		return nil, err
	}
	if err := rows.Err(); err != nil {
		return nil, err
	}
	return items, nil
}

const getChatListForUser = `-- name: GetChatListForUser :many
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
LIMIT ? OFFSET ?
`

type GetChatListForUserParams struct {
	UserID string
	Limit  int32
	Offset int32
}

type GetChatListForUserRow struct {
	ChatID        string
	ChatName      sql.NullString
	ChatAvatar    sql.NullString
	ChatUpdatedAt sql.NullTime
	ChatType      string
}

func (q *Queries) GetChatListForUser(ctx context.Context, arg GetChatListForUserParams) ([]GetChatListForUserRow, error) {
	rows, err := q.db.QueryContext(ctx, getChatListForUser, arg.UserID, arg.Limit, arg.Offset)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var items []GetChatListForUserRow
	for rows.Next() {
		var i GetChatListForUserRow
		if err := rows.Scan(
			&i.ChatID,
			&i.ChatName,
			&i.ChatAvatar,
			&i.ChatUpdatedAt,
			&i.ChatType,
		); err != nil {
			return nil, err
		}
		items = append(items, i)
	}
	if err := rows.Close(); err != nil {
		return nil, err
	}
	if err := rows.Err(); err != nil {
		return nil, err
	}
	return items, nil
}

const getGroupInfo = `-- name: GetGroupInfo :one
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
GROUP BY c.id
`

type GetGroupInfoRow struct {
	Groupid        string
	GroupName      sql.NullString
	Numberofmember int64
	ListMem        sql.NullString
	ChatType       string
	ChatAvatar     sql.NullString
}

func (q *Queries) GetGroupInfo(ctx context.Context, id string) (GetGroupInfoRow, error) {
	row := q.db.QueryRowContext(ctx, getGroupInfo, id)
	var i GetGroupInfoRow
	err := row.Scan(
		&i.Groupid,
		&i.GroupName,
		&i.Numberofmember,
		&i.ListMem,
		&i.ChatType,
		&i.ChatAvatar,
	)
	return i, err
}

const getUsersInChat = `-- name: GetUsersInChat :many
SELECT 
    ui.user_id,
    ui.user_nickname,
    ui.user_avatar,
    ui.user_email
FROM chat_members cm
JOIN user_info ui ON cm.user_id = ui.user_id
WHERE cm.chat_id = ?
ORDER BY ui.user_nickname ASC
LIMIT ? OFFSET ?
`

type GetUsersInChatParams struct {
	ChatID string
	Limit  int32
	Offset int32
}

type GetUsersInChatRow struct {
	UserID       string
	UserNickname sql.NullString
	UserAvatar   sql.NullString
	UserEmail    sql.NullString
}

func (q *Queries) GetUsersInChat(ctx context.Context, arg GetUsersInChatParams) ([]GetUsersInChatRow, error) {
	rows, err := q.db.QueryContext(ctx, getUsersInChat, arg.ChatID, arg.Limit, arg.Offset)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var items []GetUsersInChatRow
	for rows.Next() {
		var i GetUsersInChatRow
		if err := rows.Scan(
			&i.UserID,
			&i.UserNickname,
			&i.UserAvatar,
			&i.UserEmail,
		); err != nil {
			return nil, err
		}
		items = append(items, i)
	}
	if err := rows.Close(); err != nil {
		return nil, err
	}
	if err := rows.Err(); err != nil {
		return nil, err
	}
	return items, nil
}

const insertChatMember = `-- name: InsertChatMember :exec
INSERT INTO chat_members (chat_id, user_id, role)
VALUES (?, ?, ?)
`

type InsertChatMemberParams struct {
	ChatID string
	UserID string
	Role   string
}

func (q *Queries) InsertChatMember(ctx context.Context, arg InsertChatMemberParams) error {
	_, err := q.db.ExecContext(ctx, insertChatMember, arg.ChatID, arg.UserID, arg.Role)
	return err
}

const updateGroupChat = `-- name: UpdateGroupChat :exec
UPDATE chats
SET group_name = ?, group_avatar = ?, updated_at = now()
WHERE id = ?
`

type UpdateGroupChatParams struct {
	GroupName   sql.NullString
	GroupAvatar sql.NullString
	ID          string
}

func (q *Queries) UpdateGroupChat(ctx context.Context, arg UpdateGroupChatParams) error {
	_, err := q.db.ExecContext(ctx, updateGroupChat, arg.GroupName, arg.GroupAvatar, arg.ID)
	return err
}
