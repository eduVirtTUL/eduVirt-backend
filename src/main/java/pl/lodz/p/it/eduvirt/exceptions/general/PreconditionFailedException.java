package pl.lodz.p.it.eduvirt.exceptions.general;

public class PreconditionFailedException extends ApplicationBaseException {
    public PreconditionFailedException() {
        super("Precondition failed", "preconditionFailed");
    }
}
