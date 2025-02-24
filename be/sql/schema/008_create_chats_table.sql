-- +goose Up
-- +goose StatementBegin
CREATE TABLE chats (
  id CHAR(36) PRIMARY KEY,
  type VARCHAR(10) NOT NULL COMMENT 'Type: private | group',
  group_name VARCHAR(50),
  group_avatar TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- +goose StatementEnd

-- +goose StatementBegin
CREATE INDEX idx_chats_id ON chats (id);
-- +goose StatementEnd

-- +goose StatementBegin
CREATE INDEX idx_chats_group_name ON chats (group_name);

-- +goose Down
-- +goose StatementBegin
DROP INDEX idx_chats_id ON chats;
-- +goose StatementEnd

-- +goose StatementBegin
DROP INDEX idx_chats_group_name ON chats;
-- +goose StatementEnd

-- +goose StatementBegin
DROP TABLE chats;
-- +goose StatementEnd
