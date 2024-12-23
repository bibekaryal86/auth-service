CREATE TABLE apps
(
    id           VARCHAR(50)  NOT NULL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL UNIQUE,
    description  TEXT         NOT NULL,
    created_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_date TIMESTAMP
);

CREATE TABLE app_user
(
    id           INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    first_name    VARCHAR(50)                                          NOT NULL,
    last_name    VARCHAR(50)                                          NOT NULL,
    email        VARCHAR(250)                                         NOT NULL UNIQUE,
    phone        VARCHAR(50),
    password     VARCHAR(250)                                         NOT NULL,
    status       VARCHAR(50)                                          NOT NULL,
    is_validated BOOLEAN                                              NOT NULL,
    created_date TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_date TIMESTAMP
);

CREATE TABLE app_user_address
(
    id           INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    app_user_id  INTEGER                                              NOT NULL,
    address_type VARCHAR(50)                                          NOT NULL,
    street       VARCHAR(255)                                         NOT NULL,
    city         VARCHAR(100)                                         NOT NULL,
    state        VARCHAR(5)                                           NOT NULL,
    country      VARCHAR(100)                                         NOT NULL,
    postal_code  VARCHAR(10)                                          NOT NULL,
    FOREIGN KEY (app_user_id) REFERENCES app_user (id),
    CONSTRAINT uc_app_user_address UNIQUE (app_user_id, address_type)
);

CREATE TABLE app_token
(
    id            INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    app_user_id   INTEGER                                              NOT NULL,
    access_token  VARCHAR(1000)                                 NOT NULL UNIQUE,
    refresh_token VARCHAR(1000)                                 NOT NULL UNIQUE,
    created_date  TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date  TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_date  TIMESTAMP,
    FOREIGN KEY (app_user_id) REFERENCES app_user (id)
);

CREATE TABLE app_role
(
    id           INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    name         VARCHAR(100)                                  NOT NULL UNIQUE,
    description  TEXT                                                 NOT NULL,
    created_date TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_date TIMESTAMP
);

CREATE TABLE app_permission
(
    id           INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    app_id       VARCHAR(50)                                          NOT NULL,
    name         VARCHAR(100)                                         NOT NULL,
    description  TEXT                                                 NOT NULL,
    created_date TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_date TIMESTAMP,
    FOREIGN KEY (app_id) REFERENCES apps (id),
    CONSTRAINT uc_app_permission UNIQUE (app_id, name)
);

CREATE TABLE app_user_app
(
    app_id        VARCHAR(50) NOT NULL,
    app_user_id   INTEGER     NOT NULL,
    assigned_date TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (app_id, app_user_id),
    FOREIGN KEY (app_id) REFERENCES apps (id),
    FOREIGN KEY (app_user_id) REFERENCES app_user (id)
);

CREATE TABLE app_user_role
(
    app_user_id   INTEGER   NOT NULL,
    app_role_id   INTEGER   NOT NULL,
    assigned_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (app_user_id, app_role_id),
    FOREIGN KEY (app_user_id) REFERENCES app_user (id),
    FOREIGN KEY (app_role_id) REFERENCES app_role (id)
);

CREATE TABLE app_role_permission
(
    app_role_id       INTEGER   NOT NULL,
    app_permission_id INTEGER   NOT NULL,
    assigned_date     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (app_role_id, app_permission_id),
    FOREIGN KEY (app_role_id) REFERENCES app_role (id),
    FOREIGN KEY (app_permission_id) REFERENCES app_permission (id)
);

CREATE TABLE audit_apps
(
    id         INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    app_id     VARCHAR(50)                                          NOT NULL,
    event_type VARCHAR(50)                                          NOT NULL,
    event_desc TEXT,
    event_data JSONB,
    created_at TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER,
    ip_address VARCHAR(50)                                          NOT NULL,
    user_agent VARCHAR(100)                                         NOT NULL,
    FOREIGN KEY (app_id) REFERENCES apps (id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES app_user (id) ON DELETE SET NULL
);

CREATE TABLE audit_app_permission
(
    id                INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    app_permission_id INTEGER,
    event_type        VARCHAR(50)                                          NOT NULL,
    event_desc        TEXT,
    event_data        JSONB,
    created_at        TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by        INTEGER,
    ip_address        VARCHAR(50)                                          NOT NULL,
    user_agent        VARCHAR(100)                                         NOT NULL,
    FOREIGN KEY (app_permission_id) REFERENCES app_permission (id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES app_user (id) ON DELETE SET NULL
);

CREATE TABLE audit_app_role
(
    id          INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    app_role_id INTEGER,
    event_type  VARCHAR(50)                                          NOT NULL,
    event_desc  TEXT,
    event_data  JSONB,
    created_at  TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by  INTEGER,
    ip_address  VARCHAR(50)                                          NOT NULL,
    user_agent  VARCHAR(250)                                         NOT NULL,
    FOREIGN KEY (app_role_id) REFERENCES app_role (id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES app_user (id) ON DELETE SET NULL
);

CREATE TABLE audit_app_user
(
    id          INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    app_user_id INTEGER,
    event_type  VARCHAR(50)                                          NOT NULL,
    event_desc  TEXT,
    event_data  JSONB,
    created_at  TIMESTAMP                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by  INTEGER,
    ip_address  VARCHAR(50)                                          NOT NULL,
    user_agent  VARCHAR(100)                                         NOT NULL,
    FOREIGN KEY (app_user_id) REFERENCES app_user (id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES app_user (id) ON DELETE SET NULL
);

-- create indexes
CREATE UNIQUE INDEX idx_app_user_phone ON app_user (phone) WHERE phone IS NOT NULL;
CREATE INDEX idx_app_user_email ON app_user (email);
CREATE INDEX idx_app_user_password ON app_user (password);
CREATE INDEX idx_app_user_deleted_date ON app_user (deleted_date);
CREATE INDEX idx_app_user_addresses_id ON app_user_address (id);
CREATE INDEX idx_app_role_id ON app_role (id);
CREATE INDEX idx_app_role_name ON app_role (name);
CREATE INDEX idx_app_permission_id ON app_permission (id);
CREATE INDEX idx_app_permission_name ON app_permission (name);
CREATE INDEX idx_audit_app_user_created_at ON audit_app_user (created_at);
CREATE INDEX idx_audit_app_role_created_at ON audit_app_role (created_at);
CREATE INDEX idx_audit_app_permission_created_at ON audit_app_permission (created_at);
CREATE INDEX idx_audit_apps_created_at ON audit_apps (created_at);
CREATE INDEX idx_audit_app_user_created_by ON audit_app_user (created_by);
CREATE INDEX idx_audit_app_role_created_by ON audit_app_role (created_by);
CREATE INDEX idx_audit_app_permission_created_by ON audit_app_permission (created_by);
CREATE INDEX idx_audit_apps_created_by ON audit_apps (created_by);
CREATE INDEX idx_audit_app_role_role_id ON audit_app_role (app_role_id);
CREATE INDEX idx_audit_app_user_user_id ON audit_app_user (app_user_id);
CREATE INDEX idx_audit_app_permission_id ON audit_app_permission (app_permission_id);
CREATE INDEX idx_audit_apps_id ON audit_apps (app_id);

-- admin table data
INSERT INTO apps (id, name, description)
VALUES ('3e4567e89b12', 'USER MANAGEMENT SYSTEM', 'MANAGE APPS, USERS, ROLES AND PERMISSIONS');

INSERT INTO app_permission (app_id, name, description)
VALUES ('3e4567e89b12', 'PERMISSION_CREATE', 'CAN ADD PERMISSION');
INSERT INTO app_permission (app_id, name, description)
VALUES ('3e4567e89b12', 'PERMISSION_READ', 'CAN VIEW PERMISSION');
INSERT INTO app_permission (app_id, name, description)
VALUES ('3e4567e89b12', 'PERMISSION_UPDATE', 'CAN EDIT PERMISSION');
INSERT INTO app_permission (app_id, name, description)
VALUES ('3e4567e89b12', 'PERMISSION_DELETE', 'CAN DELETE PERMISSION');
INSERT INTO app_permission (app_id, name, description)
VALUES ('3e4567e89b12', 'ROLE_CREATE', 'CAN ADD ROLE');
INSERT INTO app_permission (app_id, name, description)
VALUES ('3e4567e89b12', 'ROLE_READ', 'CAN VIEW ROLE');
INSERT INTO app_permission (app_id, name, description)
VALUES ('3e4567e89b12', 'ROLE_UPDATE', 'CAN EDIT ROLE');
INSERT INTO app_permission (app_id, name, description)
VALUES ('3e4567e89b12', 'ROLE_DELETE', 'CAN DELETE ROLE');
INSERT INTO app_permission (app_id, name, description)
VALUES ('3e4567e89b12', 'ROLE_PERMISSION_ASSIGN', 'CAN ADD PERMISSION TO ROLE');
INSERT INTO app_permission (app_id, name, description)
VALUES ('3e4567e89b12', 'ROLE_PERMISSION_UNASSIGN', 'CAN REMOVE PERMISSION FROM ROLE');
INSERT INTO app_permission (app_id, name, description)
VALUES ('3e4567e89b12', 'USER_CREATE', 'CAN ADD USER');
INSERT INTO app_permission (app_id, name, description)
VALUES ('3e4567e89b12', 'USER_READ', 'CAN VIEW USER');
INSERT INTO app_permission (app_id, name, description)
VALUES ('3e4567e89b12', 'USER_UPDATE', 'CAN EDIT USER');
INSERT INTO app_permission (app_id, name, description)
VALUES ('3e4567e89b12', 'USER_DELETE', 'CAN DELETE USER');
INSERT INTO app_permission (app_id, name, description)
VALUES ('3e4567e89b12', 'USER_ROLE_ASSIGN', 'CAN ADD ROLE TO USER');
INSERT INTO app_permission (app_id, name, description)
VALUES ('3e4567e89b12', 'USER_ROLE_UNASSIGN', 'CAN REMOVE ROLE FROM USER');

INSERT INTO app_role (name, description)
VALUES ('SUPERUSER', 'USER HAS ALL AND UNLIMITED ACCESS');
INSERT INTO app_role (name, description)
VALUES ('POWERUSER', 'USER HAS ACCESS TO ALL PARTS OF THE APP, REF TYPES IS VIEW ONLY');
INSERT INTO app_role (name, description)
VALUES ('STANDARD', 'USER HAS NO ACCESS TO REF DATA');
INSERT INTO app_role (name, description)
VALUES ('GUEST', 'USER HAS VIEW ONLY ACCESS, NO REF DATA');

INSERT INTO app_role_permission (app_role_id, app_permission_id)
VALUES (2, 2);  -- POWERUSER, PERMISSION_READ
INSERT INTO app_role_permission (app_role_id, app_permission_id)
VALUES (2, 6);  -- POWERUSER, ROLE_READ
INSERT INTO app_role_permission (app_role_id, app_permission_id)
VALUES (2, 12); -- POWERUSER, USER_READ

-- DROP TABLE public.audit_apps CASCADE;
-- DROP TABLE public.audit_app_user CASCADE;
-- DROP TABLE public.audit_app_role CASCADE;
-- DROP TABLE public.audit_app_permission CASCADE;
-- DROP TABLE public.app_role_permission CASCADE;
-- DROP TABLE public.app_user_role CASCADE;
-- DROP TABLE public.app_permission CASCADE;
-- DROP TABLE public.app_role CASCADE;
-- DROP TABLE public.app_token CASCADE;
-- DROP TABLE public.app_user_app CASCADE;
-- DROP TABLE public.app_user_address CASCADE;
-- DROP TABLE public.app_user CASCADE;
-- DROP TABLE public.apps CASCADE;
-- DROP table public.flyway_schema_history CASCADE;
