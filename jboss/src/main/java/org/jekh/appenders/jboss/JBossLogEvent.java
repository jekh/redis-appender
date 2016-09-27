package org.jekh.appenders.jboss;

import org.jboss.logmanager.ExtLogRecord;
import org.jekh.appenders.jul.JULLogEvent;

import java.util.Map;

public class JBossLogEvent extends JULLogEvent<ExtLogRecord> {
    public JBossLogEvent(ExtLogRecord logEvent) {
        super(logEvent);
    }

    @Override
    public Map<String, String> getMdc() {
        return logEvent.getMdcCopy();
    }

    @Override
    public String getFormattedMessage() {
        return logEvent.getFormattedMessage();
    }

    @Override
    public String getThreadName() {
        return logEvent.getThreadName();
    }

    @Override
    public StackTraceElement getSource() {
        StackTraceElement syntheticStackTraceElement = new StackTraceElement(logEvent.getSourceClassName(), logEvent.getSourceMethodName(),
                logEvent.getSourceFileName(), logEvent.getSourceLineNumber());

        return syntheticStackTraceElement;
    }
}
