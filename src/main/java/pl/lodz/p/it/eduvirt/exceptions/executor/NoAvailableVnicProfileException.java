package pl.lodz.p.it.eduvirt.exceptions.executor;

public class NoAvailableVnicProfileException extends ExecutorBaseException {

    public NoAvailableVnicProfileException() {
        super("No available vnic profile found in pool");
    }

    public NoAvailableVnicProfileException(String message) {
        super(message);
    }
}
