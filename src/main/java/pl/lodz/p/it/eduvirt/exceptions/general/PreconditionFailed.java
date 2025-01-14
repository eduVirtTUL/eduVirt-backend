package pl.lodz.p.it.eduvirt.exceptions.general;

public class PreconditionFailed extends ApplicationBaseException {
    public PreconditionFailed() {
        super("Precondition failed", "preconditionFailed");
    }
}
