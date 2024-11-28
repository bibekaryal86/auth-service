package auth.service.app.exception;

public class UserNotActiveException extends RuntimeException {
  public UserNotActiveException() {
    super("User is not active, please revalidate or reset your account!");
  }
}
