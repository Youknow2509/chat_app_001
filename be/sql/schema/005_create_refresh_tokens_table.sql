-- +goose Up
-- +goose StatementBegin
CREATE TABLE refresh_tokens (
  id CHAR(36) PRIMARY KEY,
  user_id CHAR(36) NOT NULL,
  refresh_token TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_user_id_refresh_user_info FOREIGN KEY (user_id) REFERENCES user_info (user_id) ON DELETE CASCADE
);
-- +goose StatementEnd

-- +goose StatementBegin
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
DROP INDEX idx_refresh_tokens_user_id ON refresh_tokens;
-- +goose StatementEnd

-- +goose StatementBegin
DROP TABLE refresh_tokens;
-- +goose StatementEnd
