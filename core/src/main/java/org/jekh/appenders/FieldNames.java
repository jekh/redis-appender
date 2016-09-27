package org.jekh.appenders;

public class FieldNames {
    // exception fields
    private final String thread;
    private final String level;
    private final String message;
    private final String logger;
    private final String exception;
    private final String location;
    private final String timestamp;
    private final String mdc;

    // computeLocation data field names
    private final String classField;
    private final String method;
    private final String file;
    private final String line;

    // logstash-specific capabilities
    private final String tags;

    public String getThread() {
        return thread;
    }

    public String getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public String getLogger() {
        return logger;
    }

    public String getException() {
        return exception;
    }

    public String getLocation() {
        return location;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getClassField() {
        return classField;
    }

    public String getMethod() {
        return method;
    }

    public String getFile() {
        return file;
    }

    public String getLine() {
        return line;
    }

    public String getTags() {
        return tags;
    }

    public String getMdc() {
        return mdc;
    }

    public FieldNames(String thread, String level, String message, String logger, String exception, String location, String timestamp, String mdc, String classField, String method, String file, String line, String tags) {
        this.thread = thread;
        this.level = level;
        this.message = message;
        this.logger = logger;
        this.exception = exception;
        this.location = location;
        this.timestamp = timestamp;
        this.mdc = mdc;
        this.classField = classField;
        this.method = method;
        this.file = file;
        this.line = line;
        this.tags = tags;
    }


}
