package user.management.system.app.exception;

public class CheckPermissionException extends RuntimeException {
  public CheckPermissionException(String message) {
    super("Permission Denied: " + message);
  }
}
