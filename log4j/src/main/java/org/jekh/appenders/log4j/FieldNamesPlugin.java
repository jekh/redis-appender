package org.jekh.appenders.log4j;

import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.jekh.appenders.Defaults;
import org.jekh.appenders.FieldNames;

@Plugin(name = "fieldNames", category = "Core")
public class FieldNamesPlugin {
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

    public FieldNamesPlugin(String thread, String level, String message, String logger, String exception, String location, String timestamp, String mdc, String classField, String method, String file, String line, String tags) {
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

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder implements org.apache.logging.log4j.core.util.Builder<FieldNamesPlugin> {
        @PluginBuilderAttribute
        private String thread = Defaults.THREAD_FIELD;

        @PluginBuilderAttribute
        private String level = Defaults.LEVEL_FIELD;

        @PluginBuilderAttribute
        private String message = Defaults.MESSAGE_FIELD;

        @PluginBuilderAttribute("logger")
        private String logger = Defaults.LOGGER_FIELD;

        @PluginBuilderAttribute
        private String exception = Defaults.EXCEPTION_FIELD;

        @PluginBuilderAttribute
        private String location = Defaults.LOCATION_FIELD;

        @PluginBuilderAttribute
        private String timestamp = Defaults.TIMESTAMP_FIELD;

        @PluginBuilderAttribute
        private String mdc = Defaults.MDC_FIELD;

        // location data field names
        @PluginBuilderAttribute
        private String classField = Defaults.CLASS_FIELD;

        @PluginBuilderAttribute
        private String method = Defaults.METHOD_FIELD;

        @PluginBuilderAttribute
        private String file = Defaults.FILE_FIELD;

        @PluginBuilderAttribute
        private String line = Defaults.LINE_FIELD;

        // logstash-specific capabilities
        @PluginBuilderAttribute
        private String tags = Defaults.TAGS_FIELD;

        @Override
        public FieldNamesPlugin build() {
            return new FieldNamesPlugin(thread, level, message, logger, exception, location, timestamp, mdc, classField, method, file, line, tags);
        }
    }

    public FieldNames getFieldNames() {
        return new FieldNames(thread, level, message, logger, exception, location, timestamp, mdc, classField, method, file, line, tags);
    }

}
