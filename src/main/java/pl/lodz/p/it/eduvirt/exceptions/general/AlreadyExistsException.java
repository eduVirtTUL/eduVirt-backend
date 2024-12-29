package pl.lodz.p.it.eduvirt.exceptions.general;

public class AlreadyExistsException extends ApplicationBaseException {

    public AlreadyExistsException(String message, String key) {
        super(message, key);
    }
}
