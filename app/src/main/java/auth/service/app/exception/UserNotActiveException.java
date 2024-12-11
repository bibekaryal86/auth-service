package auth.service.app.exception;

public class UserNotActiveException extends RuntimeException {
  public UserNotActiveException() {
    super("User account is locked, please reset your account!");
  }
}
