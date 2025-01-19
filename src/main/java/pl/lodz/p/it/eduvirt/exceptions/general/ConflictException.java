package pl.lodz.p.it.eduvirt.exceptions.general;

public class ConflictException extends ApplicationBaseException {

    public ConflictException(String message, String key) {
        super(message, key);
    }

    public ConflictException(ApplicationBaseException wrappedException) {
        super(wrappedException.getMessage(), wrappedException.getKey());
    }
}
