package pl.lodz.p.it.eduvirt.exceptions.metric;

import pl.lodz.p.it.eduvirt.util.I18n;

public class MetricValueNotDefinedException extends MetricBaseException {

    public MetricValueNotDefinedException() {
        super(I18n.METRIC_VALUE_NOT_DEFINED);
    }

    public MetricValueNotDefinedException(String message) {
        super(message);
    }

    public MetricValueNotDefinedException(Throwable cause) {
        super(I18n.METRIC_VALUE_NOT_DEFINED, cause);
    }

    public MetricValueNotDefinedException(String message, Throwable cause) {
        super(message, cause);
    }
}
