package user.management.system.app.model.enums;

public class StatusEnums {
  public enum AppUserStatus {
    CREATED, // user entity is created
    PENDING, // user is sent validation email
    ACTIVE, // user is validated
    VALIDATE_INIT, // user initiated re-validation
    RESET_INIT, // user initiated reset password
    RESET_EXIT, // user initiated reset password continue
    VALIDATION_ERROR, // any error when validating user
    RESET_ERROR, // any error when resetting user
    INACTIVE, // user has not logged in for a certain time
    CANCELLED, // user account is manually cancelled
    DELETED, // user account is soft deleted
    LOGIN, // user logged in
    LOGOUT, // user logged out
    FAILED_LOGIN, // user could not log in (eg: invalid credentials)
    INVALID_AUTH // user has invalid JWT token
  }

  public enum AddressTypes {
    MAILING,
    BILLING,
    SHIPPING
  }
}
