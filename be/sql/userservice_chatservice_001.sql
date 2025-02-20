CREATE TABLE `friends` (
  `user_id` UUID,
  `friend_id` UUID
);

CREATE TABLE `friend_requests` (
  `id` UUID PRIMARY KEY,
  `from_user` UUID,
  `to_user` UUID,
  `status` VARCHAR(20),
  `created_at` TIMESTAMP DEFAULT 'now()'
);

CREATE TABLE `chats` (
  `id` UUID PRIMARY KEY,
  `type` VARCHAR(10),
  `group_name` VARCHAR(100),
  `group_avatar` TEXT,
  `created_at` TIMESTAMP DEFAULT 'now()',
  `updated_at` TIMESTAMP DEFAULT 'now()'
);

CREATE TABLE `chat_members` (
  `chat_id` UUID,
  `user_id` UUID,
  `role` VARCHAR(20)
);

CREATE TABLE `notifications` (
  `id` UUID PRIMARY KEY,
  `user_id` UUID,
  `type` VARCHAR(50),
  `data` JSONB,
  `created_at` TIMESTAMP DEFAULT 'now()'
);

CREATE TABLE `user_info` (
  `user_id` UUID PRIMARY KEY NOT NULL COMMENT 'User  ID',
  `user_account` VARCHAR(255) NOT NULL COMMENT 'User  account',
  `user_nickname` VARCHAR(255) COMMENT 'User  nickname',
  `user_avatar` VARCHAR(255) COMMENT 'User  avatar',
  `user_state` TINYINT NOT NULL COMMENT 'User  state: 0-Locked, 1-Activated, 2-Not Activated',
  `user_mobile` VARCHAR(20) COMMENT 'Mobile phone number',
  `user_gender` TINYINT COMMENT 'User  gender: 0-Secret, 1-Male, 2-Female',
  `user_birthday` DATE COMMENT 'User  birthday',
  `user_email` VARCHAR(255) COMMENT 'User  email address',
  `user_is_authentication` TINYINT NOT NULL COMMENT 'Authentication status: 0-Not Authenticated, 1-Pending, 2-Authenticated, 3-Failed',
  `created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT 'Record creation time',
  `updated_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT 'Record update time'
);

CREATE TABLE `user_base` (
  `user_id` UUID PRIMARY KEY NOT NULL COMMENT 'User  ID',
  `user_account` VARCHAR(255) NOT NULL COMMENT 'User account',
  `user_password` VARCHAR(255) NOT NULL COMMENT 'User password',
  `user_salt` VARCHAR(255) NOT NULL COMMENT 'Salt for hashing',
  `user_login_time` TIMESTAMP DEFAULT null COMMENT 'Last login timestamp',
  `user_logout_time` TIMESTAMP DEFAULT null COMMENT 'Last logout timestamp',
  `user_login_ip` VARCHAR(45) DEFAULT null COMMENT 'IP address of last login',
  `user_created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT 'Creation timestamp',
  `user_updated_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT 'Update timestamp'
);

CREATE TABLE `user_verify` (
  `verify_id` INT PRIMARY KEY NOT NULL AUTO_INCREMENT COMMENT 'Verification ID',
  `verify_otp` VARCHAR(6) NOT NULL COMMENT 'One-time password',
  `verify_email` VARCHAR(255) NOT NULL COMMENT 'Verification key - email address, phone number, ....',
  `verify_email_hash` VARCHAR(255) NOT NULL COMMENT 'Hash of the verification key',
  `verify_type` INT DEFAULT 1 COMMENT 'Verification type (e.g., 1 for email)',
  `is_verified` INT COMMENT 'Verification status: 1 for verified, 0 for not verified',
  `is_deleted` INT COMMENT 'Deletion flag: 1 for deleted, 0 for not deleted',
  `verify_created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT 'Record creation time',
  `verify_updated_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT 'Record update time'
);

CREATE UNIQUE INDEX `friend_requests_index_0` ON `friend_requests` (`to_user`);

CREATE UNIQUE INDEX `chats_index_1` ON `chats` (`group_name`);

CREATE UNIQUE INDEX `chat_members_index_2` ON `chat_members` (`chat_id`, `user_id`);

CREATE UNIQUE INDEX `user_info_index_3` ON `user_info` (`user_account`, `user_email`);

CREATE UNIQUE INDEX `user_base_index_4` ON `user_base` (`user_account`);

CREATE UNIQUE INDEX `user_verify_index_5` ON `user_verify` (`verify_otp`, `verify_email`, `is_verified`);

ALTER TABLE `friends` COMMENT = 'User friendships';

ALTER TABLE `friend_requests` COMMENT = 'pending | accepted | rejected';

ALTER TABLE `chats` COMMENT = 'private | group';

ALTER TABLE `chat_members` COMMENT = 'admin | member';

ALTER TABLE `friends` ADD FOREIGN KEY (`user_id`) REFERENCES `user_info` (`user_id`);

ALTER TABLE `friends` ADD FOREIGN KEY (`friend_id`) REFERENCES `user_info` (`user_id`);

ALTER TABLE `friend_requests` ADD FOREIGN KEY (`from_user`) REFERENCES `user_info` (`user_id`);

ALTER TABLE `friend_requests` ADD FOREIGN KEY (`to_user`) REFERENCES `user_info` (`user_id`);

ALTER TABLE `chat_members` ADD FOREIGN KEY (`chat_id`) REFERENCES `chats` (`id`);

ALTER TABLE `chat_members` ADD FOREIGN KEY (`user_id`) REFERENCES `user_info` (`user_id`);

ALTER TABLE `notifications` ADD FOREIGN KEY (`user_id`) REFERENCES `user_info` (`user_id`);

ALTER TABLE `user_base` ADD FOREIGN KEY (`user_account`) REFERENCES `user_verify` (`verify_email`);

ALTER TABLE `user_base` ADD FOREIGN KEY (`user_account`) REFERENCES `user_info` (`user_account`);
