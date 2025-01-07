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

CREATE TABLE address_type
(
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    type_name   VARCHAR(100)                                         NOT NULL UNIQUE,
    type_desc   TEXT                                                 NOT NULL,
    created_date  TIMESTAMP                                          NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date  TIMESTAMP                                          NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_date  TIMESTAMP
);

CREATE TABLE profile_address
(
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    profile_id  BIGINT                                              NOT NULL,
    type_id     BIGINT                                              NOT NULL,
    street      VARCHAR(250)                                         NOT NULL,
    city        VARCHAR(100)                                         NOT NULL,
    state       VARCHAR(5)                                           NOT NULL,
    country     VARCHAR(100)                                         NOT NULL,
    postal_code VARCHAR(10)                                          NOT NULL,
    created_date  TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date  TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_date  TIMESTAMP,
    FOREIGN KEY (profile_id) REFERENCES profile (id),
    FOREIGN KEY (type_id) REFERENCES address_type (id),
    CONSTRAINT uc_profile_and_address UNIQUE (profile_id, type_id)
);

CREATE TABLE token
(
    id            BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    platform_id   BIGINT                                              NOT NULL,
    profile_id    BIGINT                                              NOT NULL,
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
    permission_name VARCHAR(100)                                         NOT NULL UNIQUE,
    permission_desc     TEXT                                                 NOT NULL,
    created_date    TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date    TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_date    TIMESTAMP
);

CREATE TABLE platform_profile_role
(
    platform_id   BIGINT                                              NOT NULL,
    profile_id    BIGINT                                              NOT NULL,
    role_id       BIGINT                                              NOT NULL,
    assigned_date TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (platform_id, profile_id, role_id),
    FOREIGN KEY (platform_id) REFERENCES platform (id),
    FOREIGN KEY (profile_id) REFERENCES profile (id),
    FOREIGN KEY (role_id) REFERENCES role (id)
);

CREATE TABLE platform_role_permission
(
    platform_id   BIGINT                                              NOT NULL,
    role_id       BIGINT                                              NOT NULL,
    permission_id BIGINT                                              NOT NULL,
    assigned_date TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (platform_id, role_id, permission_id),
    FOREIGN KEY (platform_id) REFERENCES platform (id),
    FOREIGN KEY (role_id) REFERENCES role (id),
    FOREIGN KEY (permission_id) REFERENCES permission (id)
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
    user_agent    VARCHAR(250)                                         NOT NULL,
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
    user_agent VARCHAR(250)                                         NOT NULL,
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
    user_agent VARCHAR(100)                                         NOT NULL,
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
    user_agent  VARCHAR(100)                                         NOT NULL,
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
INSERT INTO address_type (type_name, type_desc) VALUES
('Mailing', 'Mailing Address');
INSERT INTO address_type (type_name, type_desc) VALUES
('Billing', 'Billing Address');
INSERT INTO address_type (type_name, type_desc) VALUES
('Shipping', 'Shipping Address');
-- not in fixtures
INSERT INTO address_type (type_name, type_desc) VALUES
('Mailing-1', '1-Mailing Address');
INSERT INTO address_type (type_name, type_desc) VALUES
('Billing-1', '1-Billing Address');
INSERT INTO address_type (type_name, type_desc) VALUES
('Shipping-1', '1-Shipping Address');

INSERT INTO platform (platform_name, platform_desc)
VALUES ('Auth Service', 'Authentication / Authorization Server with Roles and Permissions');
INSERT INTO platform (platform_name, platform_desc)
VALUES ('Env Service', 'Platform to hold runtime variables for different services');
INSERT INTO platform (platform_name, platform_desc)
VALUES ('Personal Expenses Tracking System', 'Application to manage / budget personal finances');
-- not in fixtures
INSERT INTO platform (platform_name, platform_desc)
VALUES ('Auth Service-1', '1-Authentication / Authorization Server with Roles and Permissions');
INSERT INTO platform (platform_name, platform_desc)
VALUES ('Env Service-1', '1-Platform to hold runtime variables for different services');
INSERT INTO platform (platform_name, platform_desc)
VALUES ('Personal Expenses Tracking System-1', '1-Application to manage / budget personal finances');

INSERT INTO profile (first_name, last_name, email, phone, password, is_validated, login_attempts, last_login, deleted_date)
VALUES ('First One', 'Last One', 'firstlast@one.com', null, 'password-one', true, 0, null, null);
INSERT INTO profile (first_name, last_name, email, phone, password, is_validated, login_attempts, last_login, deleted_date)
VALUES ('First Two', 'Last Two', 'firstlast@two.com', null, 'password-two', true, 0, null, null);
INSERT INTO profile (first_name, last_name, email, phone, password, is_validated, login_attempts, last_login, deleted_date)
VALUES ('First Three', 'Last Three', 'firstlast@three.com', null, 'password-three', true, 0, null, null);
-- not in fixtures
INSERT INTO profile (first_name, last_name, email, phone, password, is_validated, login_attempts, last_login, deleted_date)
VALUES ('First One-1', 'Last One-1', 'firstlast-1@one.com', null, 'password-one-1', true, 0, null, null);
INSERT INTO profile (first_name, last_name, email, phone, password, is_validated, login_attempts, last_login, deleted_date)
VALUES ('First Two-1', 'Last Two-1', 'firstlast-1@two.com', null, 'password-two-1', true, 0, null, null);
INSERT INTO profile (first_name, last_name, email, phone, password, is_validated, login_attempts, last_login,deleted_date)
VALUES ('First Three-1', 'Last Three-1', 'firstlast-1@three.com', null, 'password-three-1', true, 0, null, null);

INSERT INTO profile_address (profile_id, type_id, street, city, state, country, postal_code)
VALUES (1, 1, 'Street One One', 'City One One', 'ON', 'US', '12569');
INSERT INTO profile_address (profile_id, type_id, street, city, state, country, postal_code)
VALUES (1, 3, 'Street One Two', 'City One Two', 'ON', 'US', '12569');
INSERT INTO profile_address (profile_id, type_id, street, city, state, country, postal_code)
VALUES (2, 1, 'Street Two One', 'City Two One', 'TW', 'US', '96521');
-- not in fixtures
INSERT INTO profile_address (profile_id, type_id, street, city, state, country, postal_code)
VALUES (2, 2, 'Street One One-1', 'City One One-1', 'ON-1', 'US-1', '12569-1');
INSERT INTO profile_address (profile_id, type_id, street, city, state, country, postal_code)
VALUES (3, 1, 'Street One Two-1', 'City One Two-1', 'ON-1', 'US-1', '12659-1');
INSERT INTO profile_address (profile_id, type_id, street, city, state, country, postal_code)
VALUES (3, 2, 'Street Two One-1', 'City Two One-1', 'TW-1', 'US-1', '96521-1');

INSERT INTO role (role_name, role_desc)
VALUES ('SUPERUSER', 'User has all and unlimited access, including hard delete');
INSERT INTO role (role_name, role_desc)
VALUES ('POWERUSER', 'User has all access, including soft delete. View access to ref/admin data');
INSERT INTO role (role_name, role_desc)
VALUES ('GUEST', 'User has create, read and update access. No access to ref/admin data');
-- not in fixtures
INSERT INTO role (role_name, role_desc)
VALUES ('SUPERUSER-1', '1-User has all and unlimited access, including hard delete');
INSERT INTO role (role_name, role_desc)
VALUES ('POWERUSER-1', '1-User has all access, including soft delete. View access to ref/admin data');
INSERT INTO role (role_name, role_desc)
VALUES ('GUEST-1', '1-User has create, read and update access. No access to ref/admin data');

INSERT INTO permission (permission_name, permission_desc)
VALUES ('PERMISSION_CREATE', 'Can Add Permission');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('PERMISSION_READ', 'Can View Permission(s)');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('PERMISSION_UPDATE', 'Can Update Permission');
-- not in fixtures
INSERT INTO permission (permission_name, permission_desc)
VALUES ('PERMISSION_CREATE-1', '1-Can Add Permission');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('PERMISSION_READ-1', '1-Can View Permission(s)');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('PERMISSION_UPDATE-1', '1-Can Update Permission');

INSERT INTO platform_profile_role (platform_id, profile_id, role_id)
VALUES (1, 1, 1);
INSERT INTO platform_profile_role (platform_id, profile_id, role_id)
VALUES (2, 2, 2);
INSERT INTO platform_profile_role (platform_id, profile_id, role_id)
VALUES (3, 3, 3);
-- not in fixtures
INSERT INTO platform_profile_role (platform_id, profile_id, role_id)
VALUES (4, 4, 4);
INSERT INTO platform_profile_role (platform_id, profile_id, role_id)
VALUES (4, 4, 5);
INSERT INTO platform_profile_role (platform_id, profile_id, role_id)
VALUES (4, 4, 6);

INSERT INTO platform_role_permission (platform_id, role_id, permission_id)
VALUES (1, 1, 1);
INSERT INTO platform_role_permission (platform_id, role_id, permission_id)
VALUES (2, 2, 2);
INSERT INTO platform_role_permission (platform_id, role_id, permission_id)
VALUES (3, 3, 3);
-- not in fixtures
INSERT INTO platform_role_permission (platform_id, role_id, permission_id)
VALUES (4, 4, 4);
INSERT INTO platform_role_permission (platform_id, role_id, permission_id)
VALUES (4, 4, 5);
INSERT INTO platform_role_permission (platform_id, role_id, permission_id)
VALUES (4, 4, 6);
