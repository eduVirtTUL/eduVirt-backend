package pl.lodz.p.it.eduvirt.util.connection;

import org.ovirt.engine.sdk4.types.Statistic;
import org.ovirt.engine.sdk4.types.Value;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class StatisticsUtil {

    public static Optional<Statistic> getStatistic(String name, List<Statistic> statisticList) {
        for (Statistic statistic : statisticList) {
            if (statistic.name().equals(name)) {
                return Optional.of(statistic);
            }
        }
        return Optional.empty();
    }

    public static List<BigDecimal> extractStatisticValues(Statistic statistic) {
        return statistic.values().stream().map(Value::datum).toList();
    }

    public static List<BigDecimal> getStatisticValues(String statisticName, List<Statistic> statisticList) {
        Optional<Statistic> statistic = getStatistic(statisticName, statisticList);
        if (statistic.isPresent()) {
            return extractStatisticValues(statistic.get());
        }
        return new LinkedList<>();
    }

    public static Optional<BigDecimal> getStatisticSingleValue(String statisticName, List<Statistic> statisticList) {
        List<BigDecimal> values = getStatisticValues(statisticName, statisticList);
        return values.isEmpty() ? Optional.empty() : Optional.of(values.getFirst());
    }
}
