-- +goose Up
-- +goose StatementBegin
CREATE TABLE user_verify (
    `verify_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Verification ID',
    `verify_otp` VARCHAR(6) NOT NULL COMMENT 'One-time password',
    `verify_key` VARCHAR(255) NOT NULL COMMENT 'Verification key - email address, phone number, ....',
    `verify_key_hash` VARCHAR(255) NOT NULL COMMENT 'Hash of the verification key',
    `verify_type` INT NULL DEFAULT 1 COMMENT 'Verification type (e.g., 1 for email)',
    `is_verified` INT NULL COMMENT 'Verification status: 1 for verified, 0 for not verified',
    `is_deleted` INT NULL COMMENT 'Deletion flag: 1 for deleted, 0 for not deleted',
    `verify_created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation time',
    `verify_updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Record update time',
    CONSTRAINT idx_verify_otp_email_verified UNIQUE (verify_otp, verify_key, is_verified)
);
-- +goose StatementEnd

-- +goose StatementBegin
CREATE INDEX idx_user_verify_id ON user_verify (verify_id);
-- +goose StatementEnd

-- +goose StatementBegin
CREATE INDEX idx_user_verify_verify_key ON user_verify (verify_key);
-- +goose StatementEnd

-- +goose StatementBegin
CREATE INDEX idx_user_verify_verify_key_hash ON user_verify (verify_key_hash);
-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
DROP INDEX idx_user_verify_id ON user_verify;
-- +goose StatementEnd

-- +goose StatementBegin
DROP INDEX idx_user_verify_verify_key ON user_verify;
DROP INDEX idx_user_verify_verify_key_hash ON user_verify;
-- +goose StatementEnd

-- +goose StatementBegin
DROP TABLE user_verify;
-- +goose StatementEnd
