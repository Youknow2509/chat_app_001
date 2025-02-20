-- +goose Up
-- +goose StatementBegin
CREATE TABLE friend_requests (
  id CHAR(36) PRIMARY KEY,
  from_user CHAR(36),
  to_user CHAR(36),
  status VARCHAR(20),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_from_user_info FOREIGN KEY (from_user) REFERENCES user_info (user_id) ON DELETE CASCADE,
  CONSTRAINT fk_to_user_info FOREIGN KEY (to_user) REFERENCES user_info (user_id) ON DELETE CASCADE
);
-- +goose StatementEnd

-- +goose StatementBegin
CREATE INDEX idx_friend_requests_status ON friend_requests (status);
-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
DROP INDEX idx_friend_requests_status ON friend_requests;
-- +goose StatementEnd

-- +goose StatementBegin
DROP TABLE friend_requests;
-- +goose StatementEnd
