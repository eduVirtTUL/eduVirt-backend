package pl.lodz.p.it.eduvirt.exceptions.general;

import lombok.Getter;

@Getter
public class ApplicationBaseException extends RuntimeException {

    private final String key;

    public ApplicationBaseException(String key) {
        this.key = key;
    }

    public ApplicationBaseException(String message, String key) {
        super(message);
        this.key = key;
    }
}
