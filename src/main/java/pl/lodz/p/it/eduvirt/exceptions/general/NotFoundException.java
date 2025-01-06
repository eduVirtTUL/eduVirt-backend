package pl.lodz.p.it.eduvirt.exceptions.general;

public class NotFoundException extends ApplicationBaseException {

    public NotFoundException(String message, String key) {
        super(message, key);
    }
}
