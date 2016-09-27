package org.jekh.appenders.exception;

/**
 * Indicates that an error occurred when initializing the logger. The error may have occurred when initializing any component,
 * including the appender, layout, redis client, etc.
 */
public class LoggerInitializationException extends RuntimeException {
    public LoggerInitializationException() {
        super();
    }

    public LoggerInitializationException(String message) {
        super(message);
    }

    public LoggerInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoggerInitializationException(Throwable cause) {
        super(cause);
    }
}
