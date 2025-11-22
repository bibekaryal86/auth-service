package auth.service.app.exception;

public class TokenInvalidException extends RuntimeException {
  public TokenInvalidException(final String message) {
    super(message);
  }
}
