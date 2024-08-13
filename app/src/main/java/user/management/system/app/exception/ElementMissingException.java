package user.management.system.app.exception;

public class ElementMissingException extends RuntimeException {
  public ElementMissingException(final String entity, final String variable) {
    super(String.format("[%s] is Missing in [%s] request", entity, variable));
  }
}
