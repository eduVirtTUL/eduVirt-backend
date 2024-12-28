package pl.lodz.p.it.eduvirt.exceptions;

public class AccessKeyNotFoundException extends RuntimeException {
    public AccessKeyNotFoundException(String keyValue) {
        super("Access key not found with value: " + keyValue);
    }
}
