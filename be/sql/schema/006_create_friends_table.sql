-- +goose Up
-- +goose StatementBegin
CREATE TABLE friends (
  user_id CHAR(36),
  friend_id CHAR(36),
  CONSTRAINT fk_friends_user_id_user_info_1 FOREIGN KEY (user_id) REFERENCES user_info (user_id) ON DELETE CASCADE,
  CONSTRAINT fk_friends_user_id_user_info_2 FOREIGN KEY (friend_id) REFERENCES user_info (user_id) ON DELETE CASCADE
);
-- +goose StatementEnd

-- +goose StatementBegin
CREATE INDEX idx_friends_user_id ON friends (user_id);
-- +goose StatementEnd

-- +goose StatementBegin
CREATE INDEX idx_friends_friend_id ON friends (friend_id);
-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
DROP INDEX idx_friends_user_id ON friends;
-- +goose StatementEnd

-- +goose StatementBegin
DROP INDEX idx_friends_friend_id ON friends;
-- +goose StatementEnd

-- +goose StatementBegin
DROP TABLE friends;
-- +goose StatementEnd
