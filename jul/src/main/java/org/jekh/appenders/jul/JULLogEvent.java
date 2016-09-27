package org.jekh.appenders.jul;

import org.jekh.appenders.GenericLogEvent;
import org.jekh.appenders.exception.ExceptionUtil;
import org.jekh.appenders.jul.util.JULMdcHelper;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.logging.LogRecord;

/**
 * A {@link GenericLogEvent} for java.util.logging implementations. This is genericized to allow use with both out-of-the-box
 * java.util.logging as well as JBoss logging, which extends the JUL classes.
 *
 * @param <T> {@link LogRecord} implementation
 */
public class JULLogEvent<T extends LogRecord> implements GenericLogEvent {
    protected final T logEvent;

    public JULLogEvent(T logEvent) {
        this.logEvent = logEvent;
    }

    @Override
    public Map<String, String> getMdc() {
        Map<String, String> mdc = JULMdcHelper.getMdc();

        // GenericLogEvent specifies that mdc should never be null, even if the underlying logger does not support an MDC
        if (mdc == null) {
            return Collections.emptyMap();
        } else {
            return mdc;
        }
    }

    @Override
    public String getFormattedMessage() {
        return logEvent.getMessage();
    }

    @Override
    public Instant getTimestamp() {
        return Instant.ofEpochMilli(logEvent.getMillis());
    }

    @Override
    public String getLoggerName() {
        return logEvent.getLoggerName();
    }

    @Override
    public String getLevelAsString() {
        return logEvent.getLevel().getLocalizedName();
    }

    @Override
    public String getThreadName() {
        // out-of-the-box JUL doesn't provide access to the thread name, only the ID
        return Integer.toString(logEvent.getThreadID());
    }

    @Override
    public String getExceptionAsString() {
        Throwable exception = logEvent.getThrown();

        if (exception == null) {
            return null;
        }

        String exceptionAsString = ExceptionUtil.getStackTraceAsString(exception);

        return exceptionAsString;
    }

    @Override
    public StackTraceElement getSource() {
        StackTraceElement syntheticStackTraceElement = new StackTraceElement(logEvent.getSourceClassName(), logEvent.getSourceMethodName(), null, -1);

        return syntheticStackTraceElement;
    }
}
