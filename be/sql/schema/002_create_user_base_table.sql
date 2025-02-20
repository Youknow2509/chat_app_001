-- +goose Up
-- +goose StatementBegin
CREATE TABLE user_base (
  user_id CHAR(36) PRIMARY KEY NOT NULL COMMENT 'User ID',
  user_account VARCHAR(100) NOT NULL COMMENT 'User account',
  user_password VARCHAR(255) NOT NULL COMMENT 'User password',
  user_salt VARCHAR(255) NOT NULL COMMENT 'Salt for hashing',
  user_is_refresh_token INT DEFAULT 0 COMMENT 'Refresh token status: 0 true | 1 false',
  user_login_time TIMESTAMP DEFAULT NULL COMMENT 'Last login timestamp',
  user_logout_time TIMESTAMP DEFAULT NULL COMMENT 'Last logout timestamp',
  user_login_ip VARCHAR(45) DEFAULT NULL COMMENT 'IP address of last login',
  user_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
  user_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Update timestamp',
  CONSTRAINT fk_user_base_verify FOREIGN KEY (user_account) REFERENCES user_verify (verify_key)
);
-- +goose StatementEnd

-- +goose StatementBegin
CREATE INDEX idx_user_base_user_account ON user_base (user_account);
-- +goose StatementEnd

-- +goose StatementBegin
CREATE INDEX idx_user_base_user_login_ip ON user_base (user_login_ip);
-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
DROP INDEX idx_user_base_user_account ON user_base;
-- +goose StatementEnd

-- +goose StatementBegin
DROP INDEX idx_user_base_user_login_ip ON user_base;
-- +goose StatementEnd

-- +goose StatementBegin
DROP TABLE user_base;
-- +goose StatementEnd
