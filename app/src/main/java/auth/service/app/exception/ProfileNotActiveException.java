package auth.service.app.exception;

public class ProfileNotActiveException extends RuntimeException {
  public ProfileNotActiveException() {
    super("Profile is not active, please revalidate or reset your account!");
  }
}
