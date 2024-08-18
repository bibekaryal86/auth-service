package user.management.system.app.exception;

public class JwtInvalidException extends RuntimeException {
  public JwtInvalidException(String message) {
    super(message);
  }
}
