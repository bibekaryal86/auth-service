package user.management.system.app.exception;

public class ElementNotFoundException extends RuntimeException {
  public ElementNotFoundException(final String entity, final String column) {
    super(String.format("%s Not Found for [%s]", entity, column));
  }
}
