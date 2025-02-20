-- +goose Up
-- +goose StatementBegin
CREATE TABLE user_info (
  user_id CHAR(36) PRIMARY KEY NOT NULL COMMENT 'User ID',
  user_account VARCHAR(100) NOT NULL COMMENT 'User account',
  user_nickname VARCHAR(255) COMMENT 'User nickname',
  user_avatar VARCHAR(255) COMMENT 'User avatar',
  user_state ENUM('Locked', 'Activated', 'Not Activated') NOT NULL COMMENT 'User state',
  user_mobile VARCHAR(20) COMMENT 'Mobile phone number',
  user_gender ENUM('Secret', 'Male', 'Female') COMMENT 'User gender',
  user_birthday DATE COMMENT 'User birthday',
  user_email VARCHAR(100) COMMENT 'User email address',
  user_is_authentication TINYINT DEFAULT 1 COMMENT 'Authentication status: 1 false | 0 true',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation time',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record update time',
  CONSTRAINT idx_user_account_email UNIQUE (user_account, user_email)
);
-- +goose StatementEnd

-- +goose StatementBegin
CREATE INDEX idx_user_info_nickname ON user_info (user_nickname);
-- +goose StatementEnd

-- +goose StatementBegin
CREATE INDEX idx_user_info_email ON user_info (user_email);
-- +goose StatementEnd

-- +goose StatementBegin
CREATE INDEX idx_user_info_user_state ON user_info (user_state);
-- +goose StatementEnd

-- +goose StatementBegin
CREATE INDEX idx_user_info_user_is_authentication ON user_info (user_is_authentication);
-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
DROP INDEX idx_user_info_nickname ON user_info;
-- +goose StatementEnd

-- +goose StatementBegin
DROP INDEX idx_user_info_email ON user_info;
-- +goose StatementEnd

-- +goose StatementBegin
DROP INDEX idx_user_info_user_state ON user_info;
-- +goose StatementEnd

-- +goose StatementBegin
DROP INDEX idx_user_info_user_is_authentication ON user_info;
-- +goose StatementEnd

-- +goose StatementBegin
DROP TABLE user_info;
-- +goose StatementEnd
