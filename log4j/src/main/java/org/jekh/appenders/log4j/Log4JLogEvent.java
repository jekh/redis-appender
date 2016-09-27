package org.jekh.appenders.log4j;

import org.apache.logging.log4j.core.LogEvent;
import org.jekh.appenders.GenericLogEvent;

import java.time.Instant;
import java.util.Map;

public class Log4JLogEvent implements GenericLogEvent {
    private final LogEvent logEvent;

    public Log4JLogEvent(LogEvent logEvent) {
        this.logEvent = logEvent;
    }

    @Override
    public Map<String, String> getMdc() {
        return logEvent.getContextMap();
    }

    @Override
    public String getFormattedMessage() {
        return logEvent.getMessage().getFormattedMessage();
    }

    @Override
    public Instant getTimestamp() {
        return Instant.ofEpochMilli(logEvent.getTimeMillis());
    }

    @Override
    public String getLoggerName() {
        return logEvent.getLoggerName();
    }

    @Override
    public String getLevelAsString() {
        return logEvent.getLevel().toString();
    }

    @Override
    public String getThreadName() {
        return logEvent.getThreadName();
    }

    @Override
    public String getExceptionAsString() {
        if (logEvent.getThrownProxy() == null) {
            return null;
        } else {
            return logEvent.getThrownProxy().getExtendedStackTraceAsString();
        }
    }

    @Override
    public StackTraceElement getSource() {
        return logEvent.getSource();
    }
}
