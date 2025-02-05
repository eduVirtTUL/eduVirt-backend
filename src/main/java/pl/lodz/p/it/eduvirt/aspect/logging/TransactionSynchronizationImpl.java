package pl.lodz.p.it.eduvirt.aspect.logging;

import org.slf4j.Logger;
import org.springframework.transaction.support.TransactionSynchronization;

public class TransactionSynchronizationImpl implements TransactionSynchronization {

    private final String key;
    private final Logger logger;

    public TransactionSynchronizationImpl(String key, Logger logger) {
        this.key = key;
        this.logger = logger;
    }

    @Override
    public void afterCompletion(int status) {
        String mappedStatus = switch (status) {
            case STATUS_COMMITTED -> "COMMITED";
            case STATUS_ROLLED_BACK -> "ROLLED_BACK";
            default -> "UNKNOWN";
        };

        logger.info("TRANSACTION: COMPLETE | id: {} | status: {}", key, mappedStatus);
    }
}
