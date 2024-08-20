package user.management.system.app.exception;

public class ElementNotActiveException extends RuntimeException {
  public ElementNotActiveException(final String type, final String entity) {
    super(String.format("Active %s Not Found for [%s]", type, entity));
  }
}
