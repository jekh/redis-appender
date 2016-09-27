package org.jekh.appenders.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import org.jekh.appenders.GenericLogEvent;

import java.time.Instant;
import java.util.Map;

public class LogbackLogEvent implements GenericLogEvent {
    private final ILoggingEvent logbackEvent;
    private final int locationDepth;

    public LogbackLogEvent(ILoggingEvent logbackEvent, int locationDepth) {
        this.logbackEvent = logbackEvent;
        this.locationDepth = locationDepth;
    }

    @Override
    public Map<String, String> getMdc() {
        return logbackEvent.getMDCPropertyMap();
    }

    @Override
    public String getFormattedMessage() {
        return logbackEvent.getFormattedMessage();
    }

    @Override
    public Instant getTimestamp() {
        return Instant.ofEpochMilli(logbackEvent.getTimeStamp());
    }

    @Override
    public String getLoggerName() {
        return logbackEvent.getLoggerName();
    }

    @Override
    public String getLevelAsString() {
        return logbackEvent.getLevel().toString();
    }

    @Override
    public String getThreadName() {
        return logbackEvent.getThreadName();
    }

    @Override
    public String getExceptionAsString() {
        IThrowableProxy exceptionProxy = logbackEvent.getThrowableProxy();
        if (exceptionProxy == null) {
            return null;
        }

        return ThrowableProxyUtil.asString(exceptionProxy);
    }

    @Override
    public StackTraceElement getSource() {
        StackTraceElement[] locationStackTrace = logbackEvent.getCallerData();
        if (locationStackTrace.length > locationDepth) {
            return locationStackTrace[locationDepth];
        }

        return null;
    }
}
