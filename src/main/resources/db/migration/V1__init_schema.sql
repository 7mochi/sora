CREATE TABLE users
(
    id                INT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    username          VARCHAR(15)  NOT NULL,
    safe_username     VARCHAR(15)  NOT NULL,
    email             VARCHAR(254) NOT NULL,
    password_bcrypt   VARCHAR(60)  NOT NULL,
    country           VARCHAR(2)   NOT NULL DEFAULT 'XX',
    group_identifier  VARCHAR(32)  NOT NULL DEFAULT 'default',
    osu_subscriber    BOOLEAN      NOT NULL DEFAULT FALSE,
    preferred_mode    INT          NOT NULL DEFAULT 0,
    registration_time DATETIME(6)  NOT NULL,
    latest_activity   DATETIME(6)  NOT NULL,
    silence_end       DATETIME(6)  NULL,
    restriction_time  DATETIME(6)  NULL,
    CONSTRAINT uc_users_username UNIQUE (username),
    CONSTRAINT uc_users_safe_username UNIQUE (safe_username),
    CONSTRAINT uc_users_email UNIQUE (email)
);

CREATE TABLE user_statistics
(
    user_id       INT     NOT NULL,
    mode          TINYINT NOT NULL,
    ranked_score  BIGINT  NOT NULL DEFAULT 0,
    total_score   BIGINT  NOT NULL DEFAULT 0,
    pp            FLOAT   NOT NULL DEFAULT 0,
    accuracy      FLOAT   NOT NULL DEFAULT 100,
    play_count    INT     NOT NULL DEFAULT 0,
    play_time     INT     NOT NULL DEFAULT 0,
    total_hits    BIGINT  NOT NULL DEFAULT 0,
    maximum_combo INT     NOT NULL DEFAULT 0,
    global_rank   INT     NULL,
    country_rank  INT     NULL,
    grade_ss      INT     NOT NULL DEFAULT 0,
    grade_ssh     INT     NOT NULL DEFAULT 0,
    grade_s       INT     NOT NULL DEFAULT 0,
    grade_sh      INT     NOT NULL DEFAULT 0,
    grade_a       INT     NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, mode),
    CONSTRAINT fk_user_statistics_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE oauth_access_tokens
(
    id           VARCHAR(36)     NOT NULL PRIMARY KEY,
    user_id      INT          NOT NULL,
    client_id    INT          NOT NULL,
    access_token VARCHAR(64)     NOT NULL,
    scopes       VARCHAR(255) NOT NULL DEFAULT '*',
    revoked      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   DATETIME(6)  NOT NULL,
    expires_at   DATETIME(6)  NOT NULL,
    CONSTRAINT uc_oauth_access_token UNIQUE (access_token),
    INDEX idx_oauth_access_user_id (user_id),
    INDEX idx_oauth_access_expires_at (expires_at),
    CONSTRAINT fk_oauth_access_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE oauth_refresh_tokens
(
    id              VARCHAR(36)    NOT NULL PRIMARY KEY,
    access_token_id VARCHAR(36)    NOT NULL,
    refresh_token   VARCHAR(64)    NOT NULL,
    revoked         BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at      DATETIME(6) NOT NULL,
    expires_at      DATETIME(6) NOT NULL,
    CONSTRAINT uc_oauth_refresh_token UNIQUE (refresh_token),
    INDEX idx_oauth_refresh_access_token_id (access_token_id),
    INDEX idx_oauth_refresh_expires_at (expires_at),
    CONSTRAINT fk_oauth_refresh_access FOREIGN KEY (access_token_id) REFERENCES oauth_access_tokens (id) ON DELETE CASCADE
);

CREATE TABLE bancho_sessions
(
    id           VARCHAR(36)     NOT NULL PRIMARY KEY,
    user_id      INT          NOT NULL,
    access_token VARCHAR(64)     NOT NULL,
    created_at   DATETIME(6)  NOT NULL,
    expires_at   DATETIME(6)  NOT NULL,
    ip_address   VARCHAR(45)  NOT NULL,
    user_agent   VARCHAR(255) NULL,
    CONSTRAINT uc_bancho_session_token UNIQUE (access_token),
    INDEX idx_bancho_sessions_user_id (user_id),
    INDEX idx_bancho_sessions_expires_at (expires_at),
    CONSTRAINT fk_bancho_sessions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
