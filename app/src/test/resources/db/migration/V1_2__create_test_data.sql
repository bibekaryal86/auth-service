INSERT INTO platform (platform_name, platform_desc, created_date, updated_date, deleted_date)
VALUES
('Platform 1', 'This is Platform One', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', NULL),
('Platform 2', 'This is Platform Two', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', NULL),
('Platform 3', 'This is Platform Three', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000'),
('Platform 4', 'This is Platform Four', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', NULL),
('Platform 5', 'This is Platform Five', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', NULL),
('Platform 6', 'This is Platform Six', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000'),
('Platform 7', 'This is Platform Seven', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', NULL),
('Platform 8', 'This is Platform Eight', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', NULL),
('Platform 9', 'This is Platform Nine', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000');

INSERT INTO profile (first_name, last_name, email, phone, password, is_validated,
                     login_attempts, last_login, created_date, updated_date, deleted_date)
VALUES
('Profile 1', '1 Profile', 'profile@one.com', '+1-111-111-1111', 'password1', TRUE, 0, '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', NULL),
('Profile 2', '2 Profile', 'profile@two.com', NULL, 'password2', FALSE, 1, '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', NULL),
('Profile 3', '3 Profile', 'profile@three.com', '', 'password3', TRUE, 2, '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000'),
('Profile 4', '4 Profile', 'profile@four.com', '+4-444-444-4444', 'password4', TRUE, 0, '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', NULL),
('Profile 5', '5 Profile', 'profile@five.com', NULL, 'password5', TRUE, 0, '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', NULL),
('Profile 6', '6 Profile', 'profile@six.com', '', 'password6', TRUE, 3, '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000'),
('Profile 7', '7 Profile', 'profile@seven.com', '+7-777-777-7777', 'password7', TRUE, 0, '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', NULL),
('Profile 8', '8 Profile', 'profile@eight.com', NULL, 'password8', FALSE, 1, '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', NULL),
('Profile 9', '9 Profile', 'profile@nine.com', '', 'password9', TRUE, 0, '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000','2025-01-01T00:00:00.000000');

INSERT INTO profile_address
(profile_id, street, city, state, country, postal_code, created_date, updated_date, deleted_date)
VALUES
(1, '1 Street', 'CityOne', '1S', 'USA', '11111', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', NULL),
(5, '5 Street', 'CityFive', '5S', 'USA', '55555', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', NULL),
(9, '9 Street', 'CityNine', '9S', 'USA', '99999', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000');

INSERT INTO role
(role_name, role_desc, created_date, updated_date, deleted_date)
VALUES
('Role 1', 'This is Role One', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', NULL),
('Role 2', 'This is Role Two', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', NULL),
('Role 3', 'This is Role Three', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000'),
('Role 4', 'This is Role Four', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', NULL),
('Role 5', 'This is Role Five', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', NULL),
('Role 6', 'This is Role Six', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000'),
('Role 7', 'This is Role Seven', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', NULL),
('Role 8', 'This is Role Eight', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', NULL),
('Role 9', 'This is Role Nine', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000');

INSERT INTO permission
(permission_name, permission_desc, created_date, updated_date, deleted_date)
VALUES
('Permission 1', 'This is Permission One', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', NULL),
('Permission 2', 'This is Permission Two', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', NULL),
('Permission 3', 'This is Permission Three', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000'),
('Permission 4', 'This is Permission Four', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', NULL),
('Permission 5', 'This is Permission Five', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', NULL),
('Permission 6', 'This is Permission Six', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000'),
('Permission 7', 'This is Permission Seven', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', NULL),
('Permission 8', 'This is Permission Eight', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', NULL),
('Permission 9', 'This is Permission Nine', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000');

INSERT INTO platform_profile_role
(platform_id, profile_id, role_id, assigned_date, unassigned_date)
VALUES
(1, 1, 1, '2025-01-01T00:00:00.000000', NULL),
(1, 2, 2, '2025-01-01T00:00:00.000000', NULL),
(1, 3, 3, '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000'),
(5, 5, 5, '2025-01-01T00:00:00.000000', NULL),
(5, 6, 6, '2025-01-01T00:00:00.000000', NULL),
(5, 7, 7, '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000',
(9, 7, 7, '2025-01-01T00:00:00.000000', NULL),
(9, 8, 8, '2025-01-01T00:00:00.000000', NULL),
(9, 9, 9, '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000');

INSERT INTO platform_role_permission
(platform_id, role_id, permission_id, assigned_date, unassigned_date)
VALUES
(1, 1, 1, '2025-01-01T00:00:00.000000', NULL),
(1, 1, 2, '2025-01-01T00:00:00.000000', NULL),
(1, 1, 3, '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000')),
(5, 5, 5, '2025-01-01T00:00:00.000000', NULL),
(5, 5, 6, '2025-01-01T00:00:00.000000', NULL),
(5, 6, 6, '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000'),
(9, 7, 7, '2025-01-01T00:00:00.000000', NULL),
(9, 8, 8, '2025-01-01T00:00:00.000000', NULL),
(9, 9, 9, '2025-01-01T00:00:00.000000', '2025-01-01T00:00:00.000000');

INSERT INTO audit_permission (permission_id, event_type, event_desc, event_data, created_at, created_by, ip_address, user_agent)
VALUES (1, 'Permission Audit 1 One', 'This is Permission Audit One One', null, '2025-01-01T00:00:00.000000', 1, 'IP-1', 'UA-1');
INSERT INTO audit_permission (permission_id, event_type, event_desc, event_data, created_at, created_by, ip_address, user_agent)
VALUES (1, 'Permission Audit 1 Two', 'This is Permission Audit One Two', null, '2025-01-01T00:00:00.000000', 1, 'IP-1', 'UA-1');
INSERT INTO audit_permission (permission_id, event_type, event_desc, event_data, created_at, created_by, ip_address, user_agent)
VALUES (1, 'Permission Audit 1 Three', 'This is Permission Audit One Three', null, '2025-01-01T00:00:00.000000', 1, 'IP-2', 'UA-2');
INSERT INTO audit_permission (permission_id, event_type, event_desc, event_data, created_at, created_by, ip_address, user_agent)
VALUES (9, 'Permission Audit 9 One', 'This is Permission Audit Nine One', null, '2025-01-01T00:00:00.000000', 9, 'IP-9', 'UA-9');

INSERT INTO audit_role (role_id, event_type, event_desc, event_data, created_at, created_by, ip_address, user_agent)
VALUES (1, 'Role Audit 1 One', 'This is Role Audit One One', null, '2025-01-01T00:00:00.000000', 1, 'IP-1', 'UA-1');
INSERT INTO audit_role (role_id, event_type, event_desc, event_data, created_at, created_by, ip_address, user_agent)
VALUES (1, 'Role Audit 1 Two', 'This is Role Audit One Two', null, '2025-01-01T00:00:00.000000', 1, 'IP-1', 'UA-1');
INSERT INTO audit_role (role_id, event_type, event_desc, event_data, created_at, created_by, ip_address, user_agent)
VALUES (1, 'Role Audit 1 Three', 'This is Role Audit One Three', null, '2025-01-01T00:00:00.000000', 1, 'IP-2', 'UA-2');
INSERT INTO audit_role (role_id, event_type, event_desc, event_data, created_at, created_by, ip_address, user_agent)
VALUES (9, 'Role Audit 9 One', 'This is Role Audit Nine One', null, '2025-01-01T00:00:00.000000', 9, 'IP-9', 'UA-9');

INSERT INTO audit_platform (platform_id, event_type, event_desc, event_data, created_at, created_by, ip_address, user_agent)
VALUES (1, 'Platform Audit 1 One', 'This is Platform Audit One One', null, '2025-01-01T00:00:00.000000', 1, 'IP-1', 'UA-1');
INSERT INTO audit_platform (platform_id, event_type, event_desc, event_data, created_by, ip_address, user_agent)
VALUES (1, 'Platform Audit 1 Two', 'This is Platform Audit One Two', null, '2025-01-01T00:00:00.000000', 1, 'IP-1', 'UA-1');
INSERT INTO audit_platform (platform_id, event_type, event_desc, event_data, created_by, ip_address, user_agent)
VALUES (1, 'Platform Audit 1 Three', 'This is Platform Audit One Three', null, '2025-01-01T00:00:00.000000', 1, 'IP-2', 'UA-2');
INSERT INTO audit_platform (platform_id, event_type, event_desc, event_data, created_by, ip_address, user_agent)
VALUES (9, 'Platform Audit 9 One', 'This is Platform Audit Nine One', null, '2025-01-01T00:00:00.000000', 9, 'IP-9', 'UA-9');

INSERT INTO audit_profile (profile_id, event_type, event_desc, event_data, created_at, created_by, ip_address, user_agent)
VALUES (1, 'Profile Audit 1 One', 'This is Profile Audit One One', null, '2025-01-01T00:00:00.000000', 1, 'IP-1', 'UA-1');
INSERT INTO audit_profile (profile_id, event_type, event_desc, event_data, created_at, created_by, ip_address, user_agent)
VALUES (1, 'Profile Audit 1 Two', 'This is Profile Audit One Two', null, '2025-01-01T00:00:00.000000', 1, 'IP-1', 'UA-1');
INSERT INTO audit_profile (profile_id, event_type, event_desc, event_data, created_at, created_by, ip_address, user_agent)
VALUES (1, 'Profile Audit 1 Three', 'This is Profile Audit One Three', null, '2025-01-01T00:00:00.000000', 1, 'IP-2', 'UA-2');
INSERT INTO audit_profile (profile_id, event_type, event_desc, event_data, created_at, created_by, ip_address, user_agent)
VALUES (9, 'Profile Audit 9 One', 'This is Profile Audit Nine One', null, '2025-01-01T00:00:00.000000', 9, 'IP-9', 'UA-9');
