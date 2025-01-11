package pl.lodz.p.it.eduvirt.exceptions.general;

public class ForbiddenException extends ApplicationBaseException {

    public ForbiddenException(String message, String key) {
        super(message, key);
    }
}
