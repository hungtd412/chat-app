CREATE TABLE users
(
    id          CHAR(36) PRIMARY KEY,
    phone       VARCHAR(16) UNIQUE  NOT NULL,
    email       VARCHAR(255) UNIQUE NOT NULL,
    password    VARCHAR(255)         NOT NULL,
    first_name  VARCHAR(20)         NOT NULL,
    last_name   VARCHAR(20)         NOT NULL,
    is_active   BOOLEAN,
    is_blocked  BOOLEAN,
    preferences TEXT,
    created_at  DATETIME,
    updated_at  DATETIME
);

CREATE TABLE block_list
(
    id         CHAR(36) PRIMARY KEY,
    blocker_id CHAR(36) NOT NULL,
    blocked_id CHAR(36) NOT NULL,
    created_at DATETIME,
    FOREIGN KEY (blocker_id) REFERENCES users (id),
    FOREIGN KEY (blocked_id) REFERENCES users (id),
    UNIQUE KEY unique_user_participant (blocker_id, blocked_id)
);

CREATE TABLE user_verification
(
    user_id           CHAR(36) PRIMARY KEY,
    verification_code VARCHAR(45),
    created_at        VARCHAR(45),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE conversation
(
    id         CHAR(36) PRIMARY KEY,
    title      VARCHAR(40),
    creator_id CHAR(36),
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (creator_id) REFERENCES users (id)
);

CREATE TABLE participants
(
    id              CHAR(36) PRIMARY KEY,
    conversation_id CHAR(36),
    users_id        CHAR(36),
    type            ENUM('TYPE1', 'TYPE2'),
    created_at      DATETIME,
    updated_at      DATETIME,
    FOREIGN KEY (conversation_id) REFERENCES conversation (id),
    FOREIGN KEY (users_id) REFERENCES users (id)
);

CREATE TABLE messages
(
    id              CHAR(36) PRIMARY KEY,
    guid            VARCHAR(100),
    conversation_id CHAR(36),
    sender_id       CHAR(36),
    message_type    ENUM('TYPE1', 'TYPE2'),
    message         VARCHAR(255),
    created_at      DATETIME,
    deleted_at      DATETIME,
    FOREIGN KEY (conversation_id) REFERENCES conversation (id),
    FOREIGN KEY (sender_id) REFERENCES users (id)
);


CREATE TABLE attachments
(
    id          CHAR(36) PRIMARY KEY,
    messages_id CHAR(36),
    thumb_url   VARCHAR(45),
    file_url    VARCHAR(45),
    created_at  TIMESTAMP,
    updated_at  DATETIME,
    FOREIGN KEY (messages_id) REFERENCES messages (id)
);
