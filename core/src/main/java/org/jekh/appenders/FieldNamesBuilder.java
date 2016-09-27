package org.jekh.appenders;

/**
 * Builder for {@link FieldNames} objects.
 */
public class FieldNamesBuilder {
    // exception fields
    private String thread = Defaults.THREAD_FIELD;
    private String level = Defaults.LEVEL_FIELD;
    private String message = Defaults.MESSAGE_FIELD;
    private String logger = Defaults.LOGGER_FIELD;
    private String exception = Defaults.EXCEPTION_FIELD;
    private String location = Defaults.LOCATION_FIELD;
    private String timestamp = Defaults.TIMESTAMP_FIELD;
    private String mdc = Defaults.MDC_FIELD;

    // location data field names
    private String classField = Defaults.CLASS_FIELD;
    private String method = Defaults.METHOD_FIELD;
    private String file = Defaults.FILE_FIELD;
    private String line = Defaults.LINE_FIELD;

    // logstash-specific capabilities
    private String tags = Defaults.TAGS_FIELD;

    public FieldNamesBuilder setThread(String thread) {
        this.thread = thread;
        return this;
    }

    public FieldNamesBuilder setLevel(String level) {
        this.level = level;
        return this;
    }

    public FieldNamesBuilder setMessage(String message) {
        this.message = message;
        return this;
    }

    public FieldNamesBuilder setLogger(String logger) {
        this.logger = logger;
        return this;
    }

    public FieldNamesBuilder setException(String exception) {
        this.exception = exception;
        return this;
    }

    public FieldNamesBuilder setLocation(String location) {
        this.location = location;
        return this;
    }

    public FieldNamesBuilder setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public FieldNamesBuilder setMdc(String mdc) {
        this.mdc = mdc;
        return this;
    }

    public FieldNamesBuilder setClassField(String classField) {
        this.classField = classField;
        return this;
    }

    public FieldNamesBuilder setMethod(String method) {
        this.method = method;
        return this;
    }

    public FieldNamesBuilder setFile(String file) {
        this.file = file;
        return this;
    }

    public FieldNamesBuilder setLine(String line) {
        this.line = line;
        return this;
    }

    public FieldNamesBuilder setTags(String tags) {
        this.tags = tags;
        return this;
    }

    public FieldNames build() {
        return new FieldNames(thread, level, message, logger, exception, location, timestamp, mdc, classField, method, file, line, tags);
    }
}