CREATE TABLE platform
(
    id            BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    platform_name VARCHAR(100)                                         NOT NULL UNIQUE,
    platform_desc   TEXT                                                 NOT NULL,
    created_date  TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date  TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_date  TIMESTAMP
);

CREATE TABLE profile
(
    id           BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    first_name   VARCHAR(50)                                          NOT NULL,
    last_name    VARCHAR(50)                                          NOT NULL,
    email        VARCHAR(250)                                         NOT NULL UNIQUE,
    phone        VARCHAR(50),
    password     VARCHAR(500)                                         NOT NULL,
    is_validated BOOLEAN                                              NOT NULL,
    login_attempts INTEGER,
    last_login TIMESTAMP,
    created_date TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_date TIMESTAMP
);

CREATE TABLE profile_address
(
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    profile_id  BIGINT                                              NOT NULL UNIQUE,
    street      VARCHAR(250)                                         NOT NULL,
    city        VARCHAR(100)                                         NOT NULL,
    state       VARCHAR(5)                                           NOT NULL,
    country     VARCHAR(100)                                         NOT NULL,
    postal_code VARCHAR(10)                                          NOT NULL,
    created_date  TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date  TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_date  TIMESTAMP,
    FOREIGN KEY (profile_id) REFERENCES profile (id)
);

CREATE TABLE token
(
    id            BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    platform_id   BIGINT                                              NOT NULL,
    profile_id     BIGINT                                              NOT NULL,
    ip_address    VARCHAR(50)                                          NOT NULL,
    access_token  VARCHAR(1000)                                        NOT NULL UNIQUE,
    refresh_token VARCHAR(1000)                                        NOT NULL UNIQUE,
    created_date  TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date  TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_date  TIMESTAMP,
    FOREIGN KEY (platform_id) REFERENCES platform (id),
    FOREIGN KEY (profile_id) REFERENCES profile (id)
);

CREATE TABLE role
(
    id           BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    role_name    VARCHAR(100)                                         NOT NULL UNIQUE,
    role_desc  TEXT                                                 NOT NULL,
    created_date TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_date TIMESTAMP
);

CREATE TABLE permission
(
    id              BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    role_id             BIGINT NOT NULL,
    permission_name VARCHAR(100)                                         NOT NULL,
    permission_desc     TEXT                                                 NOT NULL,
    created_date    TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date    TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_date    TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES role (id),
    CONSTRAINT uc_permission_role_name UNIQUE (role_id, permission_name)
);

CREATE TABLE platform_profile_role
(
    platform_id   BIGINT                                              NOT NULL,
    profile_id    BIGINT                                              NOT NULL,
    role_id       BIGINT                                              NOT NULL,
    assigned_date TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    unassigned_date TIMESTAMP,
    PRIMARY KEY (platform_id, profile_id, role_id),
    FOREIGN KEY (platform_id) REFERENCES platform (id),
    FOREIGN KEY (profile_id) REFERENCES profile (id),
    FOREIGN KEY (role_id) REFERENCES role (id)
);

CREATE TABLE audit_permission
(
    id            BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    permission_id BIGINT,
    event_type    VARCHAR(50)                                          NOT NULL,
    event_desc    TEXT,
    event_data    JSONB,
    created_at    TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by    BIGINT,
    ip_address    VARCHAR(50)                                          NOT NULL,
    user_agent    VARCHAR(1000)                                         NOT NULL,
    FOREIGN KEY (permission_id) REFERENCES permission (id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES profile (id) ON DELETE SET NULL
);

CREATE TABLE audit_role
(
    id         BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    role_id    BIGINT,
    event_type VARCHAR(50)                                          NOT NULL,
    event_desc TEXT,
    event_data JSONB,
    created_at TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    ip_address VARCHAR(50)                                          NOT NULL,
    user_agent VARCHAR(1000)                                         NOT NULL,
    FOREIGN KEY (role_id) REFERENCES role (id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES profile (id) ON DELETE SET NULL
);

CREATE TABLE audit_profile
(
    id         BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    profile_id BIGINT,
    event_type VARCHAR(50)                                          NOT NULL,
    event_desc TEXT,
    event_data JSONB,
    created_at TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    ip_address VARCHAR(50)                                          NOT NULL,
    user_agent VARCHAR(1000)                                         NOT NULL,
    FOREIGN KEY (profile_id) REFERENCES profile (id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES profile (id) ON DELETE SET NULL
);

CREATE TABLE audit_platform
(
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    platform_id BIGINT,
    event_type  VARCHAR(50)                                          NOT NULL,
    event_desc  TEXT,
    event_data  JSONB,
    created_at  TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by  BIGINT,
    ip_address  VARCHAR(50)                                          NOT NULL,
    user_agent  VARCHAR(1000)                                         NOT NULL,
    FOREIGN KEY (platform_id) REFERENCES platform (id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES profile (id) ON DELETE SET NULL
);

-- unique indexes
-- partial index not supported in H2 database where the tests are running
--CREATE UNIQUE INDEX idx_profile_phone ON profile (phone) WHERE phone IS NOT NULL;
-- indexes
CREATE INDEX idx_platform_id ON platform (id);
CREATE INDEX idx_platform_name ON platform (platform_name);
CREATE INDEX idx_profile_id ON profile (id);
CREATE INDEX idx_profile_email ON profile (email);
CREATE INDEX idx_profile_password ON profile (password);
CREATE INDEX idx_profile_deleted_date ON profile (deleted_date);
CREATE INDEX idx_profile_address_id ON profile_address (id);
CREATE INDEX idx_role_id ON role (id);
CREATE INDEX idx_role_name ON role (role_name);
CREATE INDEX idx_permission_id ON permission (id);
CREATE INDEX idx_permission_name ON permission (permission_name);
CREATE INDEX idx_audit_platform_created_at ON audit_platform (created_at);
CREATE INDEX idx_audit_profile_created_at ON audit_profile (created_at);
CREATE INDEX idx_audit_role_created_at ON audit_role (created_at);
CREATE INDEX idx_audit_permission_created_at ON audit_permission (created_at);
CREATE INDEX idx_audit_platform_created_by ON audit_platform (created_by);
CREATE INDEX idx_audit_profile_created_by ON audit_profile (created_by);
CREATE INDEX idx_audit_role_created_by ON audit_role (created_by);
CREATE INDEX idx_audit_permission_created_by ON audit_permission (created_by);
CREATE INDEX idx_audit_platform_platform_id ON audit_platform (platform_id);
CREATE INDEX idx_audit_profile_profile_id ON audit_profile (profile_id);
CREATE INDEX idx_audit_role_role_id ON audit_role (role_id);
CREATE INDEX idx_audit_permission_permission_id ON audit_permission (permission_id);

-- create test data, match with fixtures

INSERT INTO platform (platform_name, platform_desc) VALUES ('PLATFORM-01', 'PLATFORM ONE');
INSERT INTO platform (platform_name, platform_desc) VALUES ('PLATFORM-02', 'PLATFORM TWO');
INSERT INTO platform (platform_name, platform_desc, deleted_date) VALUES ('PLATFORM-03', 'PLATFORM THREE', '2024-08-24T22:06:49.685768');
INSERT INTO platform (platform_name, platform_desc) VALUES ('PLATFORM-04', 'PLATFORM FOUR');
INSERT INTO platform (platform_name, platform_desc) VALUES ('PLATFORM-05', 'PLATFORM FIVE');
INSERT INTO platform (platform_name, platform_desc, deleted_date) VALUES ('PLATFORM-06', 'PLATFORM SIX', '2024-08-24T22:06:49.685768');
INSERT INTO platform (platform_name, platform_desc) VALUES ('PLATFORM-07', 'PLATFORM SEVEN');
INSERT INTO platform (platform_name, platform_desc) VALUES ('PLATFORM-08', 'PLATFORM EIGHT');
INSERT INTO platform (platform_name, platform_desc, deleted_date) VALUES ('PLATFORM-09', 'PLATFORM NINE', '2024-08-24T22:06:49.685768');
INSERT INTO platform (platform_name, platform_desc) VALUES ('PLATFORM-10', 'PLATFORM TEN');
INSERT INTO platform (platform_name, platform_desc) VALUES ('PLATFORM-11', 'PLATFORM ELEVEN');
INSERT INTO platform (platform_name, platform_desc, deleted_date) VALUES ('PLATFORM-12', 'PLATFORM TWELVE', '2024-08-24T22:06:49.685768');
INSERT INTO platform (platform_name, platform_desc) VALUES ('PLATFORM-13', 'PLATFORM THIRTEEN');

INSERT INTO role (role_name, role_desc) VALUES ('ROLE-01', 'ROLE ONE');
INSERT INTO role (role_name, role_desc) VALUES ('ROLE-02', 'ROLE TWO');
INSERT INTO role (role_name, role_desc, deleted_date) VALUES ('ROLE-03', 'ROLE THREE', '2024-08-24T22:06:49.685768');
INSERT INTO role (role_name, role_desc) VALUES ('ROLE-04', 'ROLE FOUR');
INSERT INTO role (role_name, role_desc) VALUES ('ROLE-05', 'ROLE FIVE');
INSERT INTO role (role_name, role_desc, deleted_date) VALUES ('ROLE-06', 'ROLE SIX', '2024-08-24T22:06:49.685768');
INSERT INTO role (role_name, role_desc) VALUES ('ROLE-07', 'ROLE SEVEN');
INSERT INTO role (role_name, role_desc) VALUES ('ROLE-08', 'ROLE EIGHT');
INSERT INTO role (role_name, role_desc, deleted_date) VALUES ('ROLE-09', 'ROLE NINE', '2024-08-24T22:06:49.685768');
INSERT INTO role (role_name, role_desc) VALUES ('ROLE-10', 'ROLE TEN');
INSERT INTO role (role_name, role_desc) VALUES ('ROLE-11', 'ROLE ELEVEN');
INSERT INTO role (role_name, role_desc, deleted_date) VALUES ('ROLE-12', 'ROLE TWELVE', '2024-08-24T22:06:49.685768');
INSERT INTO role (role_name, role_desc) VALUES ('ROLE-13', 'ROLE THIRTEEN');

INSERT INTO permission (role_id, permission_name, permission_desc) VALUES (1, 'PERMISSION-01', 'PERMISSION ONE');
INSERT INTO permission (role_id, permission_name, permission_desc) VALUES (2, 'PERMISSION-01', 'PERMISSION ONE');
INSERT INTO permission (role_id, permission_name, permission_desc, deleted_date) VALUES (1, 'PERMISSION-03', 'PERMISSION THREE', '2024-08-24T22:06:49.685768');
INSERT INTO permission (role_id, permission_name, permission_desc) VALUES (2, 'PERMISSION-04', 'PERMISSION FOUR');
INSERT INTO permission (role_id, permission_name, permission_desc) VALUES (5, 'PERMISSION-05', 'PERMISSION FIVE');
INSERT INTO permission (role_id, permission_name, permission_desc, deleted_date) VALUES (6, 'PERMISSION-06', 'PERMISSION SIX', '2024-08-24T22:06:49.685768');
INSERT INTO permission (role_id, permission_name, permission_desc) VALUES (6, 'PERMISSION-07', 'PERMISSION SEVEN');
INSERT INTO permission (role_id, permission_name, permission_desc) VALUES (10, 'PERMISSION-08', 'PERMISSION EIGHT');
INSERT INTO permission (role_id, permission_name, permission_desc, deleted_date) VALUES (10, 'PERMISSION-09', 'PERMISSION NINE', '2024-08-24T22:06:49.685768');
INSERT INTO permission (role_id, permission_name, permission_desc) VALUES (10, 'PERMISSION-10', 'PERMISSION TEN');
INSERT INTO permission (role_id, permission_name, permission_desc) VALUES (1, 'PERMISSION-11', 'PERMISSION ELEVEN');
INSERT INTO permission (role_id, permission_name, permission_desc, deleted_date) VALUES (1, 'PERMISSION-12', 'PERMISSION TWELVE', '2024-08-24T22:06:49.685768');
INSERT INTO permission (role_id, permission_name, permission_desc) VALUES (13, 'PERMISSION-13', 'PERMISSION THIRTEEN');

INSERT INTO profile (first_name, last_name, email, phone, password, is_validated, login_attempts, last_login, deleted_date)
VALUES ('First One', 'Last One', 'firstlast@one.com', '9876543210', 'password-one', true, 0, null, null);
INSERT INTO profile (first_name, last_name, email, phone, password, is_validated, login_attempts, last_login, deleted_date)
VALUES ('First Two', 'Last Two', 'firstlast@two.com', null, 'password-two', false, 0, null, null);
INSERT INTO profile (first_name, last_name, email, phone, password, is_validated, login_attempts, last_login, deleted_date)
VALUES ('First Three', 'Last Three', 'firstlast@three.com', '9876543210', 'password-three', true, 0, null, '2024-08-24T22:06:49.685768');
INSERT INTO profile (first_name, last_name, email, phone, password, is_validated, login_attempts, last_login, deleted_date)
VALUES ('First Four', 'Last Four', 'firstlast@four.com', null, 'password-four', true, 5, null, null);
INSERT INTO profile (first_name, last_name, email, phone, password, is_validated, login_attempts, last_login, deleted_date)
VALUES ('First Five', 'Last Five', 'firstlast@five.com', '9876543210', 'password-five', true, 0, null, null);
INSERT INTO profile (first_name, last_name, email, phone, password, is_validated, login_attempts, last_login, deleted_date)
VALUES ('First Six', 'Last Six', 'firstlast@six.com', '9876543210', 'password-six', true, 0, null, '2024-08-24T22:06:49.685768');
INSERT INTO profile (first_name, last_name, email, phone, password, is_validated, login_attempts, last_login, deleted_date)
VALUES ('First Seven', 'Last Seven', 'firstlast@seven.com', '9876543210', 'password-seven', true, 0, null, null);
INSERT INTO profile (first_name, last_name, email, phone, password, is_validated, login_attempts, last_login, deleted_date)
VALUES ('First Eight', 'Last Eight', 'firstlast@eight.com', null, 'password-eight', false, 0, null, null);
INSERT INTO profile (first_name, last_name, email, phone, password, is_validated, login_attempts, last_login, deleted_date)
VALUES ('First Nine', 'Last Nine', 'firstlast@nine.com', '9876543210', 'password-nine', true, 0, null, '2024-08-24T22:06:49.685768');
INSERT INTO profile (first_name, last_name, email, phone, password, is_validated, login_attempts, last_login, deleted_date)
VALUES ('First Ten', 'Last Ten', 'firstlast@ten.com', null, 'password-ten', true, 0, null, null);
INSERT INTO profile (first_name, last_name, email, phone, password, is_validated, login_attempts, last_login, deleted_date)
VALUES ('First Eleven', 'Last Eleven', 'firstlast@eleven.com', '9876543210', 'password-eleven', true, 0, null, null);
INSERT INTO profile (first_name, last_name, email, phone, password, is_validated, login_attempts, last_login, deleted_date)
VALUES ('First Twelve', 'Last Twelve', 'firstlast@twelve.com', '9876543210', 'password-twelve', true, 0, null, '2024-08-24T22:06:49.685768');
INSERT INTO profile (first_name, last_name, email, phone, password, is_validated, login_attempts, last_login, deleted_date)
VALUES ('First Thirteen', 'Last Thirteen', 'firstlast@thirteen.com', '9876543210', 'password-thirteen', true, 0, null, null);

INSERT INTO profile_address (profile_id, street, city, state, country, postal_code)
VALUES (1, 'Street-01', 'City-01', 'ST-01', 'Country-01', 'Postal-01');
INSERT INTO profile_address (profile_id, street, city, state, country, postal_code)
VALUES (2, 'Street-02', 'City-02', 'ST-02', 'Country-01', 'Postal-02');
INSERT INTO profile_address (profile_id, street, city, state, country, postal_code, deleted_date)
VALUES (3, 'Street-03', 'City-03', 'ST-03', 'Country-01', 'Postal-03', '2024-08-24T22:06:49.685768');

INSERT INTO platform_profile_role (platform_id, profile_id, role_id)
VALUES (1, 1, 1);
INSERT INTO platform_profile_role (platform_id, profile_id, role_id)
VALUES (2, 2, 2);
INSERT INTO platform_profile_role (platform_id, profile_id, role_id)
VALUES (3, 3, 3);
INSERT INTO platform_profile_role (platform_id, profile_id, role_id)
VALUES (4, 4, 4);
INSERT INTO platform_profile_role (platform_id, profile_id, role_id)
VALUES (4, 4, 5);
INSERT INTO platform_profile_role (platform_id, profile_id, role_id)
VALUES (4, 4, 6);


INSERT INTO audit_permission (permission_id, event_type, event_desc, event_data, created_by, ip_address, user_agent)
VALUES (1, 'PeET-1', 'PeED-1', null, 1, 'IP-1', 'UA-1');
INSERT INTO audit_permission (permission_id, event_type, event_desc, event_data, created_by, ip_address, user_agent)
VALUES (1, 'PeET-2', 'PeED-2', null, 1, 'IP-1', 'UA-1');
INSERT INTO audit_permission (permission_id, event_type, event_desc, event_data, created_by, ip_address, user_agent)
VALUES (1, 'PeET-2', 'PeED-2', null, 1, 'IP-2', 'UA-2');
INSERT INTO audit_permission (permission_id, event_type, event_desc, event_data, created_by, ip_address, user_agent)
VALUES (13, 'PeET-13', 'PeED-13', null, 13, 'IP-13', 'UA-13');

INSERT INTO audit_role (role_id, event_type, event_desc, event_data, created_by, ip_address, user_agent)
VALUES (1, 'RET-1', 'RED-1', null, 1, 'IP-1', 'UA-1');
INSERT INTO audit_role (role_id, event_type, event_desc, event_data, created_by, ip_address, user_agent)
VALUES (1, 'RET-2', 'RED-2', null, 1, 'IP-1', 'UA-1');
INSERT INTO audit_role (role_id, event_type, event_desc, event_data, created_by, ip_address, user_agent)
VALUES (1, 'RET-2', 'RED-2', null, 1, 'IP-2', 'UA-2');
INSERT INTO audit_role (role_id, event_type, event_desc, event_data, created_by, ip_address, user_agent)
VALUES (13, 'RET-13', 'RED-13', null, 13, 'IP-13', 'UA-13');

INSERT INTO audit_platform (platform_id, event_type, event_desc, event_data, created_by, ip_address, user_agent)
VALUES (1, 'PlET-1', 'PlED-1', null, 1, 'IP-1', 'User-Agent-1');
INSERT INTO audit_platform (platform_id, event_type, event_desc, event_data, created_by, ip_address, user_agent)
VALUES (1, 'PlET-2', 'PlED-2', null, 1, 'IP-1', 'User-Agent-1');
INSERT INTO audit_platform (platform_id, event_type, event_desc, event_data, created_by, ip_address, user_agent)
VALUES (1, 'PlET-2', 'PlED-2', null, 1, 'IP-2', 'User-Agent-2');
INSERT INTO audit_platform (platform_id, event_type, event_desc, event_data, created_by, ip_address, user_agent)
VALUES (13, 'PlET-13', 'PlED-13', null, 13, 'IP-13', 'User-Agent-13');

INSERT INTO audit_profile (profile_id, event_type, event_desc, event_data, created_by, ip_address, user_agent)
VALUES (1, 'PrET-1', 'PrED-1', null, 1, 'IP-1', 'User-Agent-1');
INSERT INTO audit_profile (profile_id, event_type, event_desc, event_data, created_by, ip_address, user_agent)
VALUES (1, 'PrET-2', 'PrED-2', null, 1, 'IP-1', 'User-Agent-1');
INSERT INTO audit_profile (profile_id, event_type, event_desc, event_data, created_by, ip_address, user_agent)
VALUES (1, 'PrET-2', 'PrED-2', null, 1, 'IP-2', 'User-Agent-2');
INSERT INTO audit_profile (profile_id, event_type, event_desc, event_data, created_by, ip_address, user_agent)
VALUES (13, 'PrET-13', 'PrED-13', null, 13, 'IP-13', 'User-Agent-13');
