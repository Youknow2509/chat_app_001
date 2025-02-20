
CREATE TABLE friends (
  user_id UUID,
  friend_id UUID,
  CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES user_info (user_id) ON DELETE CASCADE,
  CONSTRAINT fk_friend_id FOREIGN KEY (friend_id) REFERENCES user_info (user_id) ON DELETE CASCADE
);

CREATE TABLE friend_requests (
  id UUID PRIMARY KEY,
  from_user UUID,
  to_user UUID,
  status VARCHAR(20),
  created_at TIMESTAMP DEFAULT 'now()',
  CONSTRAINT fk_from_user FOREIGN KEY (from_user) REFERENCES user_info (user_id) ON DELETE CASCADE,
  CONSTRAINT fk_to_user FOREIGN KEY (to_user) REFERENCES user_info (user_id) ON DELETE CASCADE
);

CREATE TABLE chats (
  id UUID PRIMARY KEY,
  type VARCHAR(10),
  group_name VARCHAR(50),
  group_avatar TEXT,
  created_at TIMESTAMP DEFAULT 'now()',
  updated_at TIMESTAMP DEFAULT 'now()',
  CONSTRAINT idx_group_name UNIQUE (group_name)
);

CREATE TABLE chat_members (
  chat_id UUID,
  user_id UUID,
  role VARCHAR(20),
  CONSTRAINT fk_chat_id FOREIGN KEY (chat_id) REFERENCES chats (id) ON DELETE CASCADE,
  CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES user_info (user_id) ON DELETE CASCADE,
  CONSTRAINT idx_chat_members UNIQUE (chat_id, user_id)
);

CREATE TABLE notifications (
  id UUID PRIMARY KEY,
  user_id UUID,
  type VARCHAR(50),
  data JSONB,
  created_at TIMESTAMP DEFAULT 'now()',
  CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES user_info (user_id) ON DELETE CASCADE
);

CREATE TABLE user_info (
  user_id UUID PRIMARY KEY NOT NULL COMMENT 'User ID',
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

CREATE TABLE user_base (
  user_id UUID PRIMARY KEY NOT NULL COMMENT 'User ID',
  user_account VARCHAR(100) NOT NULL COMMENT 'User account',
  user_password VARCHAR(255) NOT NULL COMMENT 'User password',
  user_salt VARCHAR(255) NOT NULL COMMENT 'Salt for hashing',
  user_login_time TIMESTAMP DEFAULT null COMMENT 'Last login timestamp',
  user_logout_time TIMESTAMP DEFAULT null COMMENT 'Last logout timestamp',
  user_login_ip VARCHAR(45) DEFAULT null COMMENT 'IP address of last login',
  user_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
  user_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Update timestamp',
  CONSTRAINT fk_user_account FOREIGN KEY (user_account) REFERENCES user_verify (verify_email)
);

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

CREATE INDEX idx_friend_requests_status ON friend_requests (status);
CREATE INDEX idx_chat_members_chat_id ON chat_members (chat_id);
CREATE INDEX idx_chat_members_user_id ON chat_members (user_id);

ALTER TABLE friends COMMENT = 'User friendships';
ALTER TABLE friend_requests COMMENT = 'pending | accepted | rejected';
ALTER TABLE chats COMMENT = 'private | group';
ALTER TABLE chat_members COMMENT = 'admin | member';
ALTER TABLE notifications COMMENT = 'User notifications';
ALTER TABLE user_info COMMENT = 'User information and account details';
ALTER TABLE user_base COMMENT = 'User login and authentication details';
ALTER TABLE user_verify COMMENT = 'User verification details';
