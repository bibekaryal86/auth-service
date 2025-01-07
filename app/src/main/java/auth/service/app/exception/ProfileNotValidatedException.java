package auth.service.app.exception;

public class ProfileNotValidatedException extends RuntimeException {
  public ProfileNotValidatedException() {
    super("Profile not validated, please check your email for instructions to validate account!");
  }
}
