package user.management.system.app.model.enums;

public class AuditEnums {
  public enum AuditUsers {
    CREATE_USER,
    UPDATE_USER,
    DELETE_USER,
    ASSIGN_ROLE,
    UNASSIGN_ROLE,
    ASSIGN_APP,
    UNASSIGN_APP,
    ADD_ADDRESS,
    UPDATE_ADDRESS,
    DELETE_ADDRESS,
    USER_LOGIN,
    USER_LOGOUT,
    USER_VALIDATE_INIT,
    USER_VALIDATE_EXIT,
    USER_VALIDATE_ERROR,
    USER_RESET_INIT,
    USER_RESET_MID,
    USER_RESET_EXIT,
    USER_RESET_ERROR
  }

  public enum AuditRoles {
    CREATE_ROLE,
    UPDATE_ROLE,
    DELETE_ROLE,
    ASSIGN_PERMISSION,
    UNASSIGN_PERMISSION
  }

  public enum AuditPermissions {
    CREATE_PERMISSION,
    UPDATE_PERMISSION,
    DELETE_PERMISSION
  }
}
