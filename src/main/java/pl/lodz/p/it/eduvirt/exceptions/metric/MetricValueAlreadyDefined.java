package pl.lodz.p.it.eduvirt.exceptions.metric;

import pl.lodz.p.it.eduvirt.util.I18n;

public class MetricValueAlreadyDefined extends MetricBaseException {

    public MetricValueAlreadyDefined() {
        super(I18n.METRIC_VALUE_ALREADY_DEFINED);
    }

    public MetricValueAlreadyDefined(String message) {
        super(message);
    }

    public MetricValueAlreadyDefined(Throwable cause) {
        super(I18n.METRIC_VALUE_ALREADY_DEFINED, cause);
    }

    public MetricValueAlreadyDefined(String message, Throwable cause) {
        super(message, cause);
    }
}
