package auth.service.app.exception;

public class UserNotAuthorizedException extends RuntimeException {
  public UserNotAuthorizedException() {
    super("User Unauthorized, username and/or password not found in the system...");
  }
}
