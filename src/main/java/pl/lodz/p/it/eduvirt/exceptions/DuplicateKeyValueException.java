package pl.lodz.p.it.eduvirt.exceptions;

public class DuplicateKeyValueException extends RuntimeException {
    public DuplicateKeyValueException(String keyValue) {
        super("Access key with value '" + keyValue + "' already exists");
    }
}
