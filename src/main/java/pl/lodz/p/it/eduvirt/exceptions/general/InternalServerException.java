package pl.lodz.p.it.eduvirt.exceptions.general;

public class InternalServerException extends ApplicationBaseException {
    public InternalServerException() {
        super("Internal server error", "internalServerError");
    }
}
