package auth.service.app.exception;

public class ProfileForbiddenException extends RuntimeException {
  public ProfileForbiddenException() {
    super("Forbidden, Profile does not have permission to access this resource...");
  }
}
