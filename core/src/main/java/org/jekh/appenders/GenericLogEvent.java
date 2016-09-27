package org.jekh.appenders;

import java.time.Instant;
import java.util.Map;

public interface GenericLogEvent {
    /**
     * @return the MDC from the underlying logger implementation. Cannot return null, even if the MDC is not supported by the underlying logger.
     */
    Map<String, String> getMdc();

    String getFormattedMessage();

    Instant getTimestamp();

    String getLoggerName();

    String getLevelAsString();

    String getThreadName();

    String getExceptionAsString();

    StackTraceElement getSource();
}
