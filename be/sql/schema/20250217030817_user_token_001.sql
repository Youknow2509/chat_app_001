-- +goose Up
-- +goose StatementBegin
CREATE TABLE `user_token_001`(
    `token_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Token ID',
    `token_is_enabled` TINYINT UNSIGNED NOT NULL COMMENT 'Token status: 0-enabled, 1-disabled',
    `token_uuid_access_token` VARCHAR(255) NOT NULL COMMENT 'uuid access token in cache',
    `token_expires_in` TIMESTAMP NOT NULL COMMENT 'expires time',
    `token_created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
    `token_updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update timestamp'
)
-- +goose StatementEnd

-- +goose StatementBegin
ALTER TABLE `user_token_001`
ADD INDEX `user_token_001_token_is_enabled_index`(`token_is_enabled`);
-- +goose StatementEnd

-- +goose StatementBegin
ALTER TABLE `user_token_001`
ADD INDEX `user_token_001_token_uuid_access_token_index`(`token_uuid_access_token`);
-- +goose StatementEnd

-- +goose StatementBegin
ALTER TABLE `user_token_001`
ADD INDEX `user_token_001_token_expires_in_index`(`token_expires_in`);
-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
DROP TABLE IF EXISTS `user_token_001`;
-- +goose StatementEnd
