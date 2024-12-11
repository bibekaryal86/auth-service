package auth.service.app.exception;

public class UserLockedException extends RuntimeException {
  public UserLockedException() {
    super("User is not active, please revalidate or reset your account!");
  }
}
