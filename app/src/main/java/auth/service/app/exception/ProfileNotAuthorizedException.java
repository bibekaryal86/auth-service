package auth.service.app.exception;

public class ProfileNotAuthorizedException extends RuntimeException {
  public ProfileNotAuthorizedException() {
    super("Unauthorized Profile, email and/or password not found in the system...");
  }

  public ProfileNotAuthorizedException(final String msg) {
    super(msg);
  }
}
