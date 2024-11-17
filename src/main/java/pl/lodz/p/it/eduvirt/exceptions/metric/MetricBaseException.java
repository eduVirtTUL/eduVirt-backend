package pl.lodz.p.it.eduvirt.exceptions.metric;

import pl.lodz.p.it.eduvirt.exceptions.ApplicationBaseException;

public class MetricBaseException extends ApplicationBaseException {

    public MetricBaseException() {}

    public MetricBaseException(String message) {
        super(message);
    }

    public MetricBaseException(Throwable cause) {
        super(cause);
    }

    public MetricBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
