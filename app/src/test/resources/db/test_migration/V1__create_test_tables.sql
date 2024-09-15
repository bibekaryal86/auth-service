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
    first_name   VARCHAR(50)                                          NOT NULL,
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

-- create data (data should match the data in /fixtures)
INSERT INTO apps (id, name, description, deleted_date) VALUES ('app-1', 'App One', 'App Description One', null);
INSERT INTO apps (id, name, description, deleted_date) VALUES ('app-2', 'App Two', 'App Description Two', null);
INSERT INTO apps (id, name, description, deleted_date) VALUES ('app-3', 'App Three', 'App Description Three', null);
-- not in fixtures
INSERT INTO apps (id, name, description, deleted_date) VALUES ('app-99', 'App Ninety Nine', 'App Description Ninety Nine', null);

INSERT INTO app_user (first_name, last_name, email, phone, password, status, is_validated, deleted_date)
VALUES ('First One', 'Last One', 'firstlast@one.com', null, 'password-one', 'ACTIVE', true, null);
INSERT INTO app_user (first_name, last_name, email, phone, password, status, is_validated, deleted_date)
VALUES ('First Two', 'Last Two', 'firstlast@two.com', null, 'password-two', 'ACTIVE', true, null);
INSERT INTO app_user (first_name, last_name, email, phone, password, status, is_validated, deleted_date)
VALUES ('First Three', 'Last Three', 'firstlast@three.com', null, 'password-three', 'ACTIVE', true, null);
-- not in fixtures
INSERT INTO app_user (first_name, last_name, email, phone, password, status, is_validated, deleted_date)
VALUES ('First Ninety Nine1', 'Last Ninety Nine1', 'firstlast@ninetynine1.com', null, 'password-ninetynine1', 'ACTIVE', true, null);
INSERT INTO app_user (first_name, last_name, email, phone, password, status, is_validated, deleted_date)
VALUES ('First Ninety Nine2', 'Last Ninety Nine2', 'firstlast@ninetynine2.com', null, 'password-ninetynine2', 'ACTIVE', true, null);
INSERT INTO app_user (first_name, last_name, email, phone, password, status, is_validated, deleted_date)
VALUES ('First Ninety Nine3', 'Last Ninety Nine3', 'firstlast@ninetynine3.com', '9876543210', 'password-ninetynine3', 'ACTIVE', true, null);

INSERT INTO app_user_address (app_user_id, address_type, street, city, state, country, postal_code)
VALUES (1, 'MAILING', 'Street One One', 'City One One', 'ON', 'US', '12569');
INSERT INTO app_user_address (app_user_id, address_type, street, city, state, country, postal_code)
VALUES (1, 'SHIPPING', 'Street One Two', 'City One Two', 'ON', 'US', '12569');
INSERT INTO app_user_address (app_user_id, address_type, street, city, state, country, postal_code)
VALUES (2, 'MAILING', 'Street Two One', 'City Two One', 'TW', 'US', '96521');

INSERT INTO app_role (name, description, deleted_date) VALUES ('Role One', 'Role Description One', null);
INSERT INTO app_role (name, description, deleted_date) VALUES ('Role Two', 'Role Description Two', null);
INSERT INTO app_role (name, description, deleted_date) VALUES ('Role Three', 'Role Description Three', null);
-- not in fixtures
INSERT INTO app_role (name, description, deleted_date) VALUES ('Role A', 'Role Description A', null);
INSERT INTO app_role (name, description, deleted_date) VALUES ('Role Z', 'Role Description Z', null);
INSERT INTO app_role (name, description, deleted_date) VALUES ('Role V', 'Role Description V', null);
INSERT INTO app_role (name, description, deleted_date) VALUES ('GUEST', 'GUEST ROLE FOR CREATE USER', null);

INSERT INTO app_permission (app_id, name, description, deleted_date) VALUES ('app-1', 'Permission One', 'Permission Description One', null);
INSERT INTO app_permission (app_id, name, description, deleted_date) VALUES ('app-2', 'Permission Two', 'Permission Description Two', null);
INSERT INTO app_permission (app_id, name, description, deleted_date) VALUES ('app-3', 'Permission Three', 'Permission Description Three', null);
-- not in fixtures
INSERT INTO app_permission (app_id, name, description, deleted_date) VALUES ('app-99', 'Permission A', 'Permission Description A', null);
INSERT INTO app_permission (app_id, name, description, deleted_date) VALUES ('app-99', 'Permission Z', 'Permission Description Z', null);
INSERT INTO app_permission (app_id, name, description, deleted_date) VALUES ('app-99', 'Permission V', 'Permission Description V', null);

INSERT INTO app_user_role (app_user_id, app_role_id, assigned_date) VALUES (1, 1, CURRENT_TIMESTAMP);
INSERT INTO app_user_role (app_user_id, app_role_id, assigned_date) VALUES (2, 2, CURRENT_TIMESTAMP);
INSERT INTO app_user_role (app_user_id, app_role_id, assigned_date) VALUES (3, 3, CURRENT_TIMESTAMP);
-- not in fixtures
INSERT INTO app_user_role (app_user_id, app_role_id, assigned_date) VALUES (4, 4, CURRENT_TIMESTAMP);
INSERT INTO app_user_role (app_user_id, app_role_id, assigned_date) VALUES (4, 5, CURRENT_TIMESTAMP);
INSERT INTO app_user_role (app_user_id, app_role_id, assigned_date) VALUES (4, 6, CURRENT_TIMESTAMP);

INSERT INTO app_role_permission (app_role_id, app_permission_id, assigned_date) VALUES (1, 1, CURRENT_TIMESTAMP);
INSERT INTO app_role_permission (app_role_id, app_permission_id, assigned_date) VALUES (2, 2, CURRENT_TIMESTAMP);
INSERT INTO app_role_permission (app_role_id, app_permission_id, assigned_date) VALUES (3, 3, CURRENT_TIMESTAMP);
-- not in fixtures
INSERT INTO app_role_permission (app_role_id, app_permission_id, assigned_date) VALUES (4, 4, CURRENT_TIMESTAMP);
INSERT INTO app_role_permission (app_role_id, app_permission_id, assigned_date) VALUES (4, 5, CURRENT_TIMESTAMP);
INSERT INTO app_role_permission (app_role_id, app_permission_id, assigned_date) VALUES (4, 6, CURRENT_TIMESTAMP);

INSERT INTO app_user_app (app_id, app_user_id, assigned_date) VALUES ('app-1', 1, CURRENT_TIMESTAMP);
INSERT INTO app_user_app (app_id, app_user_id, assigned_date) VALUES ('app-2', 2, CURRENT_TIMESTAMP);
INSERT INTO app_user_app (app_id, app_user_id, assigned_date) VALUES ('app-3', 3, CURRENT_TIMESTAMP);
-- not in fixtures
INSERT INTO app_user_app (app_id, app_user_id, assigned_date) VALUES ('app-99', 4, CURRENT_TIMESTAMP);
INSERT INTO app_user_app (app_id, app_user_id, assigned_date) VALUES ('app-99', 5, CURRENT_TIMESTAMP);
INSERT INTO app_user_app (app_id, app_user_id, assigned_date) VALUES ('app-99', 6, CURRENT_TIMESTAMP);
