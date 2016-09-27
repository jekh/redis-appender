package org.jekh.appenders.exception;

/**
 * Indicates that an error occurred when initializing the logger. The error may have occurred when initializing any component,
 * including the appender, layout, redis client, etc.
 */
public class LoggerInitializationError extends RuntimeException {
    public LoggerInitializationError() {
        super();
    }

    public LoggerInitializationError(String message) {
        super(message);
    }

    public LoggerInitializationError(String message, Throwable cause) {
        super(message, cause);
    }

    public LoggerInitializationError(Throwable cause) {
        super(cause);
    }
}
