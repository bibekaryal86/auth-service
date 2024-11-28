package auth.service.app.exception;

public class ElementMissingException extends RuntimeException {
  public ElementMissingException(final String entity, final String variable) {
    super(String.format("[%s] is Missing in [%s] request", variable, entity));
  }
}
