package user.management.system.app.exception;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(final String entity) {
        super(String.format("%s Not Found", entity));
    }

    public EntityNotFoundException(final String entity, final int id) {
        super(String.format("%s Not Found for ID %s", entity, id));
    }
}
