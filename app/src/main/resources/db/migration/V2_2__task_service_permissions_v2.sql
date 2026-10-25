DELETE FROM permission WHERE permission_name LIKE 'TASKSVC_%';

INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_PERMISSION_SUPERUSER', 'Superuser Permissions');

INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_MEMBER_CREATE', 'Can Add Member');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_MEMBER_READ', 'Can View Member(s)');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_MEMBER_UPDATE', 'Can Update Member');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_MEMBER_SOFTDELETE', 'Can Soft Delete Member');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_MEMBER_HARDDELETE', 'Can Hard Delete Member');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_MEMBER_RESTORE', 'Can Restore Soft Deleted Member');

INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_CREATE', 'Can Add Task');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_READ', 'Can View Task(s)');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_UPDATE', 'Can Update Task');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_SOFTDELETE', 'Can Soft Delete Task');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_HARDDELETE', 'Can Hard Delete Task');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_RESTORE', 'Can Restore Soft Deleted Task');

INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_NOTIFICATION_CREATE', 'Can Add Notification');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_NOTIFICATION_READ', 'Can View Notification(s)');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_NOTIFICATION_UPDATE', 'Can Update Notification');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_NOTIFICATION_SOFTDELETE', 'Can Soft Delete Notification');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_NOTIFICATION_HARDDELETE', 'Can Hard Delete Notification');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_NOTIFICATION_RESTORE', 'Can Restore Soft Deleted Notification');
