package pl.lodz.p.it.eduvirt.exceptions.pod;

public class PodBaseException extends RuntimeException {

    public PodBaseException() {
    }

    public PodBaseException(String message) {
        super(message);
    }

    public PodBaseException(Throwable cause) {
        super(cause);
    }

    public PodBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
