package pl.lodz.p.it.eduvirt.exceptions.metric;

import pl.lodz.p.it.eduvirt.util.I18n;

public class MetricNotFoundException extends MetricBaseException {

    public MetricNotFoundException() {
        super(I18n.METRIC_NOT_FOUND);
    }

    public MetricNotFoundException(String message) {
        super(message);
    }

    public MetricNotFoundException(Throwable cause) {
        super(I18n.METRIC_NOT_FOUND, cause);
    }

    public MetricNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
