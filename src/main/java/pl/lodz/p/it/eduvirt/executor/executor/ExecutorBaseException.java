package pl.lodz.p.it.eduvirt.executor.executor;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ExecutorBaseException extends RuntimeException {

    public ExecutorBaseException(String message) {
        super(message);
    }

    public ExecutorBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
