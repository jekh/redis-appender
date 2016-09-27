package org.jekh.appenders.jul;

import org.jekh.appenders.Defaults;
import org.jekh.appenders.Field;
import org.jekh.appenders.FieldNames;
import org.jekh.appenders.FieldNamesBuilder;
import org.jekh.appenders.LogstashJsonFormatter;
import org.jekh.appenders.gson.GsonUtil;
import org.jekh.appenders.jul.util.JULConfigUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.LogRecord;

/**
 * A Logstash JSON formatter for use with java.util.logging. Configures itself using properties found in the logging.properties
 * configuration loaded by {@link java.util.logging.LogManager}. Formatter properties begin with the fully-qualified name
 * of this class. For example:
 * <pre>
    org.jekh.appenders.jul.JULLogstashFormatter.locationAsObject=true
    org.jekh.appenders.jul.JULLogstashFormatter.field.levelField=severity
 * </pre>
 */
public class JULLogstashFormatter extends java.util.logging.Formatter {
    private final LogstashJsonFormatter jsonLayout;

    private static final String FORMATTER_PROPERTY_BASE = JULLogstashFormatter.class.getCanonicalName();
    private static final String LOCATION_AS_OBJECT_PROPERTY = FORMATTER_PROPERTY_BASE + ".locationAsObject";
    private static final String MDC_AS_OBJECT_PROPERTY = FORMATTER_PROPERTY_BASE + ".mdcAsObject";
    private static final String TAGS_JSON_PROPERTY = FORMATTER_PROPERTY_BASE + ".tagsJson";
    private static final String ADDITIONAL_FIELDS_JSON_PROPERTY = FORMATTER_PROPERTY_BASE + ".additionalFieldsJson";
    private static final String SUPPRESS_FIELDS_JSON_PROPERTY = FORMATTER_PROPERTY_BASE + ".suppressFieldsJson";
    private static final String MDC_INCLUDE_JSON_PROPERTY = FORMATTER_PROPERTY_BASE + ".mdcIncludeJson";
    private static final String MDC_EXCLUDE_JSON_PROPERTY = FORMATTER_PROPERTY_BASE + ".mdcExcludeJson";

    private static final String FIELD_NAME_BASE = FORMATTER_PROPERTY_BASE + ".field";
    private static final String THREAD_PROPERTY = FIELD_NAME_BASE + ".thread";
    private static final String LEVEL_PROPERTY = FIELD_NAME_BASE + ".levelField";
    private static final String MESSAGE_PROPERTY = FIELD_NAME_BASE + ".message";
    private static final String LOGGER_PROPERTY = FIELD_NAME_BASE + ".logger";
    private static final String EXCEPTION_PROPERTY = FIELD_NAME_BASE + ".exception";
    private static final String LOCATION_PROPERTY = FIELD_NAME_BASE + ".location";
    private static final String TIMESTAMP_PROPERTY = FIELD_NAME_BASE + ".timestamp";
    private static final String MDC_PROPERTY = FIELD_NAME_BASE + ".mdc";
    private static final String CLASS_PROPERTY = FIELD_NAME_BASE + ".class";
    private static final String METHOD_PROPERTY = FIELD_NAME_BASE + ".method";
    private static final String FILE_PROPERTY = FIELD_NAME_BASE + ".file";
    private static final String LINE_PROPERTY = FIELD_NAME_BASE + ".line";
    private static final String TAGS_PROPERTY = FIELD_NAME_BASE + ".tags";

    public JULLogstashFormatter() {
        jsonLayout = configureLayout();
    }

    @Override
    public String format(LogRecord record) {
        JULLogEvent<LogRecord> logEvent = new JULLogEvent<>(record);

        String append = jsonLayout.getString(logEvent);

        return append;
    }

    private LogstashJsonFormatter configureLayout() {
        boolean locationAsObject = JULConfigUtil.getBooleanProperty(LOCATION_AS_OBJECT_PROPERTY, Defaults.LOCATION_AS_OBJECT);
        boolean mdcAsObject = JULConfigUtil.getBooleanProperty(MDC_AS_OBJECT_PROPERTY, Defaults.MDC_AS_OBJECT);

        List<String> tags;
        String tagsJson = JULConfigUtil.getProperty(TAGS_JSON_PROPERTY, null);
        if (tagsJson == null) {
            tags = Defaults.TAGS;
        } else {
            tags = GsonUtil.GSON.fromJson(tagsJson, GsonUtil.LIST_OF_STRING_TYPE);
        }

        Map<String, String> additionalFields;
        String additionalFieldsJson = JULConfigUtil.getProperty(ADDITIONAL_FIELDS_JSON_PROPERTY, null);
        if (additionalFieldsJson == null) {
            additionalFields = Defaults.ADDITIONAL_FIELDS;
        } else {
            additionalFields = GsonUtil.GSON.fromJson(additionalFieldsJson, GsonUtil.MAP_OF_STRING_STRING_TYPE);
        }

        Set<Field> suppressFields;
        String suppressFieldsJson = JULConfigUtil.getProperty(SUPPRESS_FIELDS_JSON_PROPERTY, null);
        if (suppressFieldsJson == null) {
            suppressFields = Defaults.SUPPRESS_FIELDS;
        } else {
            suppressFields = GsonUtil.GSON.fromJson(suppressFieldsJson, GsonUtil.SET_OF_FIELD_TYPE);
        }

        Set<String> mdcInclude;
        String mdcIncludeJson = JULConfigUtil.getProperty(MDC_INCLUDE_JSON_PROPERTY, null);
        if (mdcIncludeJson == null) {
            mdcInclude = Defaults.MDC_INCLUDE;
        } else {
            mdcInclude = GsonUtil.GSON.fromJson(mdcIncludeJson, GsonUtil.SET_OF_STRING_TYPE);
        }

        Set<String> mdcExclude;
        String mdcExcludeJson = JULConfigUtil.getProperty(MDC_EXCLUDE_JSON_PROPERTY, null);
        if (mdcExcludeJson == null) {
            mdcExclude = Defaults.MDC_EXCLUDE;
        } else {
            mdcExclude = GsonUtil.GSON.fromJson(mdcExcludeJson, GsonUtil.SET_OF_STRING_TYPE);
        }

        String thread = JULConfigUtil.getProperty(THREAD_PROPERTY, Defaults.THREAD_FIELD);
        String level = JULConfigUtil.getProperty(LEVEL_PROPERTY, Defaults.LEVEL_FIELD);
        String message = JULConfigUtil.getProperty(MESSAGE_PROPERTY, Defaults.MESSAGE_FIELD);
        String logger = JULConfigUtil.getProperty(LOGGER_PROPERTY, Defaults.LOGGER_FIELD);
        String exception = JULConfigUtil.getProperty(EXCEPTION_PROPERTY, Defaults.EXCEPTION_FIELD);
        String location = JULConfigUtil.getProperty(LOCATION_PROPERTY, Defaults.LOCATION_FIELD);
        String timestamp = JULConfigUtil.getProperty(TIMESTAMP_PROPERTY, Defaults.TIMESTAMP_FIELD);
        String mdc = JULConfigUtil.getProperty(MDC_PROPERTY, Defaults.MDC_FIELD);
        String classField = JULConfigUtil.getProperty(CLASS_PROPERTY, Defaults.CLASS_FIELD);
        String method = JULConfigUtil.getProperty(METHOD_PROPERTY, Defaults.METHOD_FIELD);
        String file = JULConfigUtil.getProperty(FILE_PROPERTY, Defaults.FILE_FIELD);
        String line = JULConfigUtil.getProperty(LINE_PROPERTY, Defaults.LINE_FIELD);
        String tagsField = JULConfigUtil.getProperty(TAGS_PROPERTY, Defaults.TAGS_FIELD);

        FieldNamesBuilder fieldNamesBuilder = new FieldNamesBuilder();
        fieldNamesBuilder.setThread(thread);
        fieldNamesBuilder.setLevel(level);
        fieldNamesBuilder.setMessage(message);
        fieldNamesBuilder.setLogger(logger);
        fieldNamesBuilder.setException(exception);
        fieldNamesBuilder.setLocation(location);
        fieldNamesBuilder.setTimestamp(timestamp);
        fieldNamesBuilder.setMdc(mdc);
        fieldNamesBuilder.setClassField(classField);
        fieldNamesBuilder.setMethod(method);
        fieldNamesBuilder.setFile(file);
        fieldNamesBuilder.setLine(line);
        fieldNamesBuilder.setTags(tagsField);

        FieldNames fieldNames = fieldNamesBuilder.build();

        return new LogstashJsonFormatter(fieldNames, locationAsObject, mdcAsObject, tags, additionalFields, suppressFields, mdcInclude, mdcExclude);
    }
}
