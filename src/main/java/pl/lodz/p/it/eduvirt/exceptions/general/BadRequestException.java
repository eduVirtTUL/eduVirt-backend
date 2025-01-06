package pl.lodz.p.it.eduvirt.exceptions.general;

public class BadRequestException extends ApplicationBaseException {

    public BadRequestException(String message, String key) {
        super(message, key);
    }
}
