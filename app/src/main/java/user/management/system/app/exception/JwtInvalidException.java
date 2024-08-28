package user.management.system.app.exception;

public class JwtInvalidException extends RuntimeException {
  public JwtInvalidException(final String message) {
    super(message);
  }
}
