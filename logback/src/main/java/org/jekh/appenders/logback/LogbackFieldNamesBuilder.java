package org.jekh.appenders.logback;

import org.jekh.appenders.FieldNames;
import org.jekh.appenders.FieldNamesBuilder;

/**
 * A special wrapper for {@link FieldNamesBuilder} for use with logback, since logback's Joran configurator requires that
 * the setter fields all return void.
 */
public class LogbackFieldNamesBuilder {
    private final FieldNamesBuilder fieldNamesBuilder = new FieldNamesBuilder();

    public void setThread(String thread) {
        fieldNamesBuilder.setThread(thread);
    }

    public void setLevel(String level) {
        fieldNamesBuilder.setLevel(level);
    }

    public void setMessage(String message) {
        fieldNamesBuilder.setMessage(message);
    }

    public void setLogger(String logger) {
        fieldNamesBuilder.setLogger(logger);
    }

    public void setException(String exception) {
        fieldNamesBuilder.setException(exception);
    }

    public void setLocation(String location) {
        fieldNamesBuilder.setLocation(location);
    }

    public void setTimestamp(String timestamp) {
        fieldNamesBuilder.setTimestamp(timestamp);
    }

    public void setMdc(String mdc) {
        fieldNamesBuilder.setMdc(mdc);
    }

    public void setClassField(String classField) {
        fieldNamesBuilder.setClassField(classField);
    }

    public void setMethod(String method) {
        fieldNamesBuilder.setMethod(method);
    }

    public void setFile(String file) {
        fieldNamesBuilder.setFile(file);
    }

    public void setLine(String line) {
        fieldNamesBuilder.setLine(line);
    }

    public void setTags(String tags) {
        fieldNamesBuilder.setTags(tags);
    }

    public FieldNames build() {
        return fieldNamesBuilder.build();
    }
}
