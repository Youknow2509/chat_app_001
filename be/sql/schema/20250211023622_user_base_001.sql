-- +goose Up
-- +goose StatementBegin
CREATE TABLE `user_base_001` (
    `user_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'User ID',
    `user_account` VARCHAR(255) NOT NULL COMMENT 'User account',
    `user_password` VARCHAR(255) NOT NULL COMMENT 'User password',
    `user_salt` VARCHAR(255) NOT NULL COMMENT 'Salt for hashing',
    `user_login_time` TIMESTAMP NULL DEFAULT NULL COMMENT 'Last login timestamp',
    `user_logout_time` TIMESTAMP NULL DEFAULT NULL COMMENT 'Last logout timestamp',
    `user_is_refresh_token_enabled` TINYINT UNSIGNED NOT NULL COMMENT 'Status user have refresh token: 0-enabled, 1-disabled',
    `user_login_ip` VARCHAR(45) NULL DEFAULT NULL COMMENT 'IP address of last login',
    `user_created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
    `user_updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update timestamp'
);
-- +goose StatementEnd

-- +goose StatementBegin
ALTER TABLE `user_base_001`
ADD UNIQUE `user_base_001_user_account_unique`(`user_account`);
-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
DROP TABLE IF EXISTS `user_base_001`;
-- +goose StatementEnd