CREATE TABLE address_types (
    id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT NOT NULL
);

CREATE TABLE user_status (
    id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT NOT NULL
);

CREATE TABLE project_status (
    id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT NOT NULL
);

CREATE TABLE users (
    id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(250) UNIQUE NOT NULL,
    phone VARCHAR(50),
    password VARCHAR(250) NOT NULL,
    status_id INTEGER NOT NULL,
    is_validated BOOLEAN NOT NULL,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_date TIMESTAMP,
    FOREIGN KEY (status_id) REFERENCES user_status(id)
);

CREATE TABLE users_addresses (
    id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    user_id INTEGER NOT NULL,
    address_type_id INTEGER NOT NULL,
    address_id INTEGER NOT NULL,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(5) NOT NULL,
    country VARCHAR(100) NOT NULL,
    postal_code VARCHAR(10) NOT NULL,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (address_type_id) REFERENCES address_types(id)
);

CREATE TABLE roles (
    id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT NOT NULL,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE permissions (
    id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT NOT NULL,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE projects (
    id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT NOT NULL,
    status_id INTEGER NOT NULL,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    repo VARCHAR(255),
    link VARCHAR(255),
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_date TIMESTAMP,
    FOREIGN KEY (status_id) REFERENCES project_status(id)
);

CREATE TABLE users_roles (
    user_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,
    assigned_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE users_projects_roles (
    user_id INTEGER NOT NULL,
    project_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,
    assigned_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, project_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE roles_permissions (
    role_id INTEGER NOT NULL,
    permission_id INTEGER NOT NULL,
    assigned_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (permission_id) REFERENCES permissions(id)
);

CREATE TABLE audit_users (
    id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    user_id INTEGER,
    event_type VARCHAR(50) NOT NULL,
    event_data JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER NOT NULL,
    ip_address VARCHAR(50) NOT NULL,
    user_agent VARCHAR(100) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE audit_roles (
    id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    role_id INTEGER,
    event_type VARCHAR(50) NOT NULL,
    event_data JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER NOT NULL,
    ip_address VARCHAR(50) NOT NULL,
    user_agent VARCHAR(100) NOT NULL,
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE audit_permissions (
    id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    permission_id INTEGER,
    event_type VARCHAR(50) NOT NULL,
    event_data JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER NOT NULL,
    ip_address VARCHAR(50) NOT NULL,
    user_agent VARCHAR(100) NOT NULL,
    FOREIGN KEY (permission_id) REFERENCES permissions(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE audit_projects (
    id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    project_id INTEGER,
    event_type VARCHAR(50) NOT NULL,
    event_data JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER NOT NULL,
    ip_address VARCHAR(50) NOT NULL,
    user_agent VARCHAR(100) NOT NULL,
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- create indexes
CREATE UNIQUE INDEX idx_users_phone ON users(phone) WHERE phone IS NOT NULL;
CREATE index idx_address_type_id ON address_types(id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_password ON users(password);
CREATE INDEX idx_users_deleted_date ON users(deleted_date);
CREATE INDEX idx_users_addresses_id ON users_addresses(id);
CREATE INDEX idx_user_status_id ON user_status(id);
CREATE INDEX idx_roles_id ON roles(id);
CREATE INDEX idx_roles_name ON roles(name);
CREATE INDEX idx_permissions_id ON permissions(id);
CREATE INDEX idx_permissions_name ON permissions(name);
CREATE INDEX idx_projects_id ON projects(id);
CREATE INDEX idx_projects_name ON projects(name);
CREATE INDEX idx_projects_deleted_date ON projects(deleted_date);
CREATE INDEX idx_project_status_id ON project_status(id);
CREATE INDEX idx_audit_users_created_at ON audit_users(created_at);
CREATE INDEX idx_audit_roles_created_at ON audit_roles(created_at);
CREATE INDEX idx_audit_permissions_created_at ON audit_permissions(created_at);
CREATE INDEX idx_audit_projects_created_at ON audit_projects(created_at);
CREATE INDEX idx_audit_users_created_by ON audit_users(created_by);
CREATE INDEX idx_audit_roles_created_by ON audit_roles(created_by);
CREATE INDEX idx_audit_permissions_created_by ON audit_permissions(created_by);
CREATE INDEX idx_audit_projects_created_by ON audit_projects(created_by);
CREATE INDEX idx_audit_users_user_id ON audit_users(user_id);
CREATE INDEX idx_audit_roles_role_id ON audit_roles(role_id);
CREATE INDEX idx_audit_permissions_permission_id ON audit_permissions(permission_id);
CREATE INDEX idx_audit_projects_project_id ON audit_projects(project_id);

-- admin table data
INSERT INTO address_types (name, description) VALUES ('MAILING', 'MAILING ADDRESS OF USER (E.G. FOR LETTERS OR POSTAL MAIL)');
INSERT INTO address_types (name, description) VALUES ('SHIPPING', 'SHIPPING ADDRESS OF USER (E.G. FOR DELIVERIES OR PACKAGES)');
INSERT INTO address_types (name, description) VALUES ('BILLING', 'BILLING ADDRESS OF USER (E.G. FOR INVOICES OR PAYMENTS)');

INSERT INTO user_status (name, description) VALUES ('ACTIVE', 'THE USER ACCOUNT IS ACTIVE AND CAN LOG IN TO THE SYSTEM');
INSERT INTO user_status (name, description) VALUES ('PENDING', 'THE USER ACCOUNT IS PENDING APPROVAL OR VALIDATION');
INSERT INTO user_status (name, description) VALUES ('VALIDATE_INIT', 'THE USER ACCOUNT IS PENDING VALIDATION, VALIDATION EMAIL SENT');
INSERT INTO user_status (name, description) VALUES ('VALIDATE_ERROR', 'THE USER ACCOUNT IS NOT VALIDATED, ISSUE DURING VALIDATION');
INSERT INTO user_status (name, description) VALUES ('RESET_INIT', 'USER REQUESTED TO RESET PASSWORD, RESET EMAIL SENT');
INSERT INTO user_status (name, description) VALUES ('RESET_ERROR', 'USER ACCOUNT IS NOT RESET, ISSUE DURING RESET');
INSERT INTO user_status (name, description) VALUES ('INACTIVE', 'USER ACCOUNT IS INACTIVE AND CANNOT LOG IN TO THE SYSTEM');
INSERT INTO user_status (name, description) VALUES ('BLOCKED', 'USER ACCOUNT IS BLOCKED AND CANNOT LOG IN TO THE SYSTEM');
INSERT INTO user_status (name, description) VALUES ('DELETED', 'USER ACCOUNT IS SOFT DELETED AND VERY HARD TO RESTORE');

INSERT INTO project_status (name, description) VALUES ('PROPOSED', 'THE PROJECT IS PROPOSED AND AWAITING APPROVAL');
INSERT INTO project_status (name, description) VALUES ('REJECTED', 'THE PROJECT IS REJECTED DUE TO VARIOUS REASONS');
INSERT INTO project_status (name, description) VALUES ('APPROVED', 'THE PROJECT IS APPROVED AND READY TO START');
INSERT INTO project_status (name, description) VALUES ('IN_PROGRESS', 'THE PROJECT IS CURRENTLY IN PROGRESS');
INSERT INTO project_status (name, description) VALUES ('ON_HOLD', 'THE PROJECT IS ON HOLD DUE TO VARIOUS REASONS');
INSERT INTO project_status (name, description) VALUES ('DELAYED', 'THE PROJECT IS DELAYED DUE TO VARIOUS REASONS');
INSERT INTO project_status (name, description) VALUES ('CANCELLED', 'THE PROJECT IS CANCELLED DUE TO VARIOUS REASONS');
INSERT INTO project_status (name, description) VALUES ('COMPLETED', 'THE PROJECT IS COMPLETED AND WORK IS DONE');
INSERT INTO project_status (name, description) VALUES ('DELETED', 'THE PROJECT IS SOFT DELETED AND VERY HARD TO RESTORE');

INSERT INTO permissions (name, description) VALUES ('ADDRESS_TYPE_CREATE', 'CAN ADD ADDRESS TYPE');
INSERT INTO permissions (name, description) VALUES ('ADDRESS_TYPE_READ', 'CAN VIEW ADDRESS TYPE');
INSERT INTO permissions (name, description) VALUES ('ADDRESS_TYPE_UPDATE', 'CAN EDIT ADDRESS TYPE');
INSERT INTO permissions (name, description) VALUES ('ADDRESS_TYPE_DELETE', 'CAN DELETE ADDRESS TYPE');
INSERT INTO permissions (name, description) VALUES ('PERMISSION_CREATE', 'CAN ADD PERMISSION');
INSERT INTO permissions (name, description) VALUES ('PERMISSION_READ', 'CAN VIEW PERMISSION');
INSERT INTO permissions (name, description) VALUES ('PERMISSION_UPDATE', 'CAN EDIT PERMISSION');
INSERT INTO permissions (name, description) VALUES ('PERMISSION_DELETE', 'CAN DELETE PERMISSION');
INSERT INTO permissions (name, description) VALUES ('PROJECT_CREATE', 'CAN ADD PROJECT');
INSERT INTO permissions (name, description) VALUES ('PROJECT_READ', 'CAN VIEW PROJECT');
INSERT INTO permissions (name, description) VALUES ('PROJECT_UPDATE', 'CAN EDIT PROJECT');
INSERT INTO permissions (name, description) VALUES ('PROJECT_DELETE', 'CAN DELETE PROJECT');
INSERT INTO permissions (name, description) VALUES ('PROJECT_STATUS_CREATE', 'CAN ADD PROJECT STATUS');
INSERT INTO permissions (name, description) VALUES ('PROJECT_STATUS_READ', 'CAN VIEW PROJECT STATUS');
INSERT INTO permissions (name, description) VALUES ('PROJECT_STATUS_UPDATE', 'CAN EDIT PROJECT STATUS');
INSERT INTO permissions (name, description) VALUES ('PROJECT_STATUS_DELETE', 'CAN DELETE PROJECT STATUS');
INSERT INTO permissions (name, description) VALUES ('ROLE_CREATE', 'CAN ADD ROLE');
INSERT INTO permissions (name, description) VALUES ('ROLE_READ', 'CAN VIEW ROLE');
INSERT INTO permissions (name, description) VALUES ('ROLE_UPDATE', 'CAN EDIT ROLE');
INSERT INTO permissions (name, description) VALUES ('ROLE_DELETE', 'CAN DELETE ROLE');
INSERT INTO permissions (name, description) VALUES ('ROLE_PERMISSION_ASSIGN', 'CAN ADD PERMISSION TO ROLE');
INSERT INTO permissions (name, description) VALUES ('ROLE_PERMISSION_UNASSIGN', 'CAN REMOVE PERMISSION FROM ROLE');
INSERT INTO permissions (name, description) VALUES ('USER_CREATE', 'CAN ADD USER');
INSERT INTO permissions (name, description) VALUES ('USER_READ', 'CAN VIEW USER');
INSERT INTO permissions (name, description) VALUES ('USER_UPDATE', 'CAN EDIT USER');
INSERT INTO permissions (name, description) VALUES ('USER_DELETE', 'CAN DELETE USER');
INSERT INTO permissions (name, description) VALUES ('USER_PROJECT_ROLE_ASSIGN', 'CAN ADD USER TO PROJECT WITH ROLE');
INSERT INTO permissions (name, description) VALUES ('USER_PROJECT_ROLE_UNASSIGN', 'CAN REMOVE USER FROM PROJECT WITH ROLE');
INSERT INTO permissions (name, description) VALUES ('USER_PROJECT_ROLE_MODIFY', 'CAN UPDATE USER ROLE IN PROJECT');
INSERT INTO permissions (name, description) VALUES ('USER_ROLE_ASSIGN', 'CAN ADD ROLE TO USER');
INSERT INTO permissions (name, description) VALUES ('USER_ROLE_UNASSIGN', 'CAN REMOVE ROLE FROM USER');
INSERT INTO permissions (name, description) VALUES ('USER_STATUS_CREATE', 'CAN ADD USER STATUS');
INSERT INTO permissions (name, description) VALUES ('USER_STATUS_READ', 'CAN VIEW USER STATUS');
INSERT INTO permissions (name, description) VALUES ('USER_STATUS_UPDATE', 'CAN EDIT USER STATUS');
INSERT INTO permissions (name, description) VALUES ('USER_STATUS_DELETE', 'CAN DELETE USER STATUS');

INSERT INTO roles (name, description) VALUES ('SUPERUSER', 'USER HAS ALL AND UNLIMITED ACCESS');
INSERT INTO roles (name, description) VALUES ('POWERUSER', 'USER HAS ACCESS TO ALL PARTS OF THE APP, REF TYPES IS VIEW ONLY');
INSERT INTO roles (name, description) VALUES ('STANDARD', 'USER HAS NO ACCESS TO REF DATA');
INSERT INTO roles (name, description) VALUES ('GUEST', 'USER HAS VIEW ONLY ACCESS, NO REF DATA');

INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 2); -- POWERUSER, ADDRESS_TYPE_READ
INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 6); -- POWERUSER, PERMISSION_READ
INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 9); -- POWERUSER, PROJECT_CREATE
INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 10); -- POWERUSER, PROJECT_READ
INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 11); -- POWERUSER, PROJECT_UPDATE
INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 12); -- POWERUSER, PROJECT_DELETE
INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 14); -- POWERUSER, PROJECT_STATUS_READ
INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 18); -- POWERUSER, ROLE_READ
INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 24); -- POWERUSER, USER_READ
INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 27); -- POWERUSER, USER_PROJECT_ROLE_ASSIGN
INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 28); -- POWERUSER, USER_PROJECT_ROLE_UNASSIGN
INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 29); -- POWERUSER, USER_PROJECT_ROLE_MODIFY
INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 33); -- POWERUSER, USER_STATUS_READ
INSERT INTO roles_permissions (role_id, permission_id) VALUES (3, 9); -- STANDARD, PROJECT_CREATE
INSERT INTO roles_permissions (role_id, permission_id) VALUES (3, 10); -- STANDARD, PROJECT_READ
INSERT INTO roles_permissions (role_id, permission_id) VALUES (3, 11); -- POWERUSER, PROJECT_UPDATE
INSERT INTO roles_permissions (role_id, permission_id) VALUES (4, 10); -- GUEST, PROJECT_READ

-- DROP table public.address_types CASCADE;
-- DROP table public.audit_permissions CASCADE;
-- DROP table public.audit_projects CASCADE;
-- DROP table public.audit_roles CASCADE;
-- DROP table public.audit_users CASCADE;
-- DROP table public.flyway_schema_history CASCADE;
-- DROP table public.permissions CASCADE;
-- DROP table public.project_status CASCADE;
-- DROP table public.projects CASCADE;
-- DROP table public.roles CASCADE;
-- DROP table public.roles_permissions CASCADE;
-- DROP table public.user_status CASCADE;
-- DROP table public.users CASCADE;
-- DROP table public.users_addresses CASCADE;
-- DROP table public.users_projects_roles CASCADE;
-- DROP table public.users_roles CASCADE;
