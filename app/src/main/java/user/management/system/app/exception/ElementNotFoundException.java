package user.management.system.app.exception;

public class ElementNotFoundException extends RuntimeException {
  public ElementNotFoundException(final String entity, final int id) {
    super(String.format("%s Not Found for ID %s", entity, id));
  }
}
