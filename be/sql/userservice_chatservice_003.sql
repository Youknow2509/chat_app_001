CREATE TABLE user_verify (
  verify_id INT PRIMARY KEY NOT NULL AUTO_INCREMENT COMMENT 'Verification ID',
  verify_otp VARCHAR(6) NOT NULL COMMENT 'One-time password',
  verify_email VARCHAR(255) NOT NULL COMMENT 'Verification key',
  is_verified INT COMMENT 'Verification status',
  is_deleted INT COMMENT 'Deletion flag',
  verify_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation time',
  verify_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record update time',
  CONSTRAINT idx_verify_otp_email_verified UNIQUE (verify_otp, verify_email, is_verified)
);
CREATE INDEX idx_user_verify_verify_email ON user_verify (verify_email);

CREATE TABLE user_base (
  user_id CHAR(36) PRIMARY KEY NOT NULL COMMENT 'User ID',
  user_account VARCHAR(100) NOT NULL COMMENT 'User account',
  user_password VARCHAR(255) NOT NULL COMMENT 'User password',
  user_salt VARCHAR(255) NOT NULL COMMENT 'Salt for hashing',
  user_is_refresh_token INT NOT NULL COMMENT 'Refresh token status',
  user_login_time TIMESTAMP DEFAULT null COMMENT 'Last login timestamp',
  user_logout_time TIMESTAMP DEFAULT null COMMENT 'Last logout timestamp',
  user_login_ip VARCHAR(45) DEFAULT null COMMENT 'IP address of last login',
  user_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
  user_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Update timestamp',
  CONSTRAINT fk_user_base_verify FOREIGN KEY (user_account) REFERENCES user_verify (verify_email)
);
CREATE INDEX idx_user_base_user_account ON user_base (user_account);
CREATE INDEX idx_user_base_user_login_ip ON user_base (user_login_ip);

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
  user_is_authentication TINYINT NOT NULL COMMENT 'Authentication status',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation time',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record update time',
  CONSTRAINT idx_user_account_email UNIQUE (user_account, user_email)
);
CREATE INDEX idx_user_info_nickname ON user_info (user_nickname);
CREATE INDEX idx_user_info_email ON user_info (user_email);
CREATE INDEX idx_user_info_user_state ON user_info (user_state);
CREATE INDEX idx_user_info_user_is_authentication ON user_info (user_is_authentication);


CREATE TABLE auth_tokens (
  id CHAR(36) PRIMARY KEY,
  user_id CHAR(36) NOT NULL,
  access_token TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_auth_tokens_user_info FOREIGN KEY (user_id) REFERENCES user_info (user_id) ON DELETE CASCADE
);
CREATE INDEX idx_auth_tokens_user_id ON auth_tokens (user_id);

CREATE TABLE refresh_tokens (
  id CHAR(36) PRIMARY KEY,
  user_id CHAR(36) NOT NULL,
  refresh_token TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_user_id_refresh_user_info FOREIGN KEY (user_id) REFERENCES user_info (user_id) ON DELETE CASCADE
);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);

CREATE TABLE friends (
  user_id CHAR(36),
  friend_id CHAR(36),
  CONSTRAINT fk_friends_user_id_user_info_1 FOREIGN KEY (user_id) REFERENCES user_info (user_id) ON DELETE CASCADE,
  CONSTRAINT fk_friends_user_id_user_info_2 FOREIGN KEY (friend_id) REFERENCES user_info (user_id) ON DELETE CASCADE
);
CREATE INDEX idx_friends_user_id ON friends (user_id);
CREATE INDEX idx_friends_friend_id ON friends (friend_id);

CREATE TABLE friend_requests (
  id CHAR(36) PRIMARY KEY,
  from_user CHAR(36),
  to_user CHAR(36),
  status VARCHAR(20),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_from_user_info FOREIGN KEY (from_user) REFERENCES user_info (user_id) ON DELETE CASCADE,
  CONSTRAINT fk_to_user_info FOREIGN KEY (to_user) REFERENCES user_info (user_id) ON DELETE CASCADE
);
CREATE INDEX idx_friend_requests_status ON friend_requests (status);

CREATE TABLE chats (
  id CHAR(36) PRIMARY KEY,
  type VARCHAR(10) NOT NULL COMMENT 'Type: private | group',
  group_name VARCHAR(50),
  group_avatar TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT idx_group_name UNIQUE (group_name)
);
CREATE INDEX idx_chats_type ON chats (type);
CREATE INDEX idx_chats_group_name ON chats (group_name);

CREATE TABLE chat_members (
  chat_id CHAR(36) NOT NULL,
  user_id CHAR(36) NOT NULL,
  role VARCHAR(20) NOT NULL COMMENT 'Role: admin | member',
  CONSTRAINT fk_chat_id FOREIGN KEY (chat_id) REFERENCES chats (id) ON DELETE CASCADE,
  CONSTRAINT fk_user_id_info FOREIGN KEY (user_id) REFERENCES user_info (user_id) ON DELETE CASCADE,
  CONSTRAINT idx_chat_members UNIQUE (chat_id, user_id)
);
CREATE INDEX idx_chat_members_chat_id ON chat_members (chat_id);
CREATE INDEX idx_chat_members_user_id ON chat_members (user_id);

CREATE TABLE notifications (
  id CHAR(36) PRIMARY KEY,
  user_id CHAR(36),
  type VARCHAR(50),
  data JSON,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES user_info (user_id) ON DELETE CASCADE
);
CREATE INDEX idx_notifications_user_id ON notifications (user_id);

