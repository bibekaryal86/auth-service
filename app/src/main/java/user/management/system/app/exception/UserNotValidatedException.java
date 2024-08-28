package user.management.system.app.exception;

public class UserNotValidatedException extends RuntimeException {
  public UserNotValidatedException() {
    super("User not validated, please check your email for instructions to validate account!");
  }
}
