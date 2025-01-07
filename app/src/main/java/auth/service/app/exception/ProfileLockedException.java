package auth.service.app.exception;

public class ProfileLockedException extends RuntimeException {
  public ProfileLockedException() {
    super("Profile is locked, please reset your account!");
  }
}
