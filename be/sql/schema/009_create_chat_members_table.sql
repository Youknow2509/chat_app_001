-- +goose Up
-- +goose StatementBegin
CREATE TABLE chat_members (
  chat_id CHAR(36) NOT NULL,
  user_id CHAR(36) NOT NULL,
  role VARCHAR(20) NOT NULL COMMENT 'Role: admin | member',
  CONSTRAINT fk_chat_id FOREIGN KEY (chat_id) REFERENCES chats (id) ON DELETE CASCADE,
  CONSTRAINT fk_user_id_info FOREIGN KEY (user_id) REFERENCES user_info (user_id) ON DELETE CASCADE,
  CONSTRAINT idx_chat_members UNIQUE (chat_id, user_id)
);
-- +goose StatementEnd

-- +goose StatementBegin
CREATE INDEX idx_chat_members_chat_id ON chat_members (chat_id);
-- +goose StatementEnd

-- +goose StatementBegin
CREATE INDEX idx_chat_members_user_id ON chat_members (user_id);
-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
DROP INDEX idx_chat_members_chat_id ON chat_members;
-- +goose StatementEnd

-- +goose StatementBegin
DROP INDEX idx_chat_members_user_id ON chat_members;
-- +goose StatementEnd

-- +goose StatementBegin
DROP TABLE chat_members;
-- +goose StatementEnd
