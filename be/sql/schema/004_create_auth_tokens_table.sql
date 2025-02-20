-- +goose Up
-- +goose StatementBegin
CREATE TABLE auth_tokens (
  id CHAR(36) PRIMARY KEY,
  user_id CHAR(36) NOT NULL,
  access_token TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_auth_tokens_user_info FOREIGN KEY (user_id) REFERENCES user_info (user_id) ON DELETE CASCADE
);
-- +goose StatementEnd

-- +goose StatementBegin
CREATE INDEX idx_auth_tokens_user_id ON auth_tokens (user_id);
-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
DROP INDEX idx_auth_tokens_user_id ON auth_tokens;
-- +goose StatementEnd

-- +goose StatementBegin
DROP TABLE auth_tokens;
-- +goose StatementEnd
