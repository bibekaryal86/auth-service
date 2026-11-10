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
VALUES ('TASKSVC_MEMBER_DELETE', 'Can Delete Member');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_MEMBER_SOFTDELETE', 'Can Soft Delete Member');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_MEMBER_RESTORE', 'Can Restore Soft Deleted Member');

INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_CREATE', 'Can Add Task');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_READ', 'Can View Task(s)');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_UPDATE', 'Can Update Task');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_DELETE', 'Can Delete Task');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_SOFTDELETE', 'Can Soft Delete Task');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_RESTORE', 'Can Restore Soft Deleted Task');

INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_INSTANCE_CREATE', 'Can Create Task Instance');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_INSTANCE_READ', 'Can View Task Instance(s)');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_INSTANCE_UPDATE', 'Can Update Task Instance');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_INSTANCE_COMPLETE', 'Can Complete Task Instance');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_INSTANCE_SKIP', 'Can Skip Task Instance');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_INSTANCE_REOPEN', 'Can Reopen Task Instance');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_INSTANCE_NOTIFY', 'Can Notify Task Instance Assignee');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_INSTANCE_DELETE', 'Can Delete Task Instance');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_INSTANCE_SOFTDELETE', 'Can Soft Delete Task Instance');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_INSTANCE_RESTORE', 'Can Restore Soft Deleted Task Instance');

INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_ITEM_CREATE', 'Can Create Task Item');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_ITEM_READ', 'Can View Task Item(s)');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_ITEM_UPDATE', 'Can Update Task Item');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_ITEM_DELETE', 'Can Delete Task Item');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_ITEM_SOFTDELETE', 'Can Soft Delete Task Item');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_ITEM_RESTORE', 'Can Restore Soft Deleted Task Item');

INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_INSTANCE_ITEM_CREATE', 'Can Create Task Instance Item');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_INSTANCE_ITEM_READ', 'Can View Task Instance Item(s)');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_INSTANCE_ITEM_UPDATE', 'Can Update Task Instance Item');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_INSTANCE_ITEM_COMPLETE', 'Can Complete Task Instance Item');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_INSTANCE_ITEM_REOPEN', 'Can Reopen Task Instance Item');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_INSTANCE_ITEM_DELETE', 'Can Delete Task Instance Item');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_INSTANCE_ITEM_SOFTDELETE', 'Can Soft Delete Task Instance Item');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_INSTANCE_ITEM_RESTORE', 'Can Restore Soft Deleted Task Instance Item');

INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_NOTIFICATION_CONFIG_CREATE', 'Can Add Notification Config');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_NOTIFICATION_CONFIG_READ', 'Can View Notification Config(s)');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_NOTIFICATION_CONFIG_UPDATE', 'Can Update Notification Config');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_NOTIFICATION_CONFIG_DELETE', 'Can Delete Notification Config');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_NOTIFICATION_CONFIG_SOFTDELETE', 'Can Soft Delete Notification Config');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_NOTIFICATION_CONFIG_RESTORE', 'Can Restore Soft Deleted Notification Config');

INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_NOTIFICATION_LOG_READ', 'Can Read Notification Log(s)');

INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_MEMBER_AUDIT_READ', 'Can Read Task Audit History');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_AUDIT_READ', 'Can Read Task Audit History');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_INSTANCE_AUDIT_READ', 'Can Read Task Instance Audit History');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_ITEM_AUDIT_READ', 'Can Read Task Item Audit History');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_TASK_INSTANCE_ITEM_AUDIT_READ', 'Can Read Task Instance Item Audit History');
INSERT INTO permission (permission_name, permission_desc)
VALUES ('TASKSVC_NOTIFICATION_CONFIG_AUDIT_READ', 'Can Read Task Instance Item Audit History');
