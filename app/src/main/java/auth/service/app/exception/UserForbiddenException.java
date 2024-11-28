package auth.service.app.exception;

public class UserForbiddenException extends RuntimeException {
  public UserForbiddenException() {
    super("Forbidden, User does not have permission to access this resource...");
  }
}
