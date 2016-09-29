package org.jekh.appenders.jboss;

import org.jboss.logmanager.ExtLogRecord;
import org.jekh.appenders.Defaults;
import org.jekh.appenders.Field;
import org.jekh.appenders.FieldNamesBuilder;
import org.jekh.appenders.LogstashJsonFormatter;
import org.jekh.appenders.exception.ExceptionUtil;
import org.jekh.appenders.exception.LoggerInitializationException;
import org.jekh.appenders.gson.GsonUtil;
import org.jekh.appenders.jul.util.JULConfigUtil;
import org.jekh.appenders.log.SimpleLog;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.LogRecord;

public class JBossLogstashFormatter extends java.util.logging.Formatter {
    private LogstashJsonFormatter jsonLayout;

    private volatile boolean configured = false;

    /**
     * Configuration failed due to an exception.
     */
    private volatile boolean configFailed = false;

    private boolean locationAsObject = Defaults.LOCATION_AS_OBJECT;

    private boolean mdcAsObject = Defaults.MDC_AS_OBJECT;

    private List<String> tags = Defaults.TAGS;

    private Map<String, String> additionalFields = Defaults.ADDITIONAL_FIELDS;

    private Set<Field> suppressFields = Defaults.SUPPRESS_FIELDS;

    private Set<String> mdcInclude = Defaults.MDC_INCLUDE;
    private Set<String> mdcExclude = Defaults.MDC_EXCLUDE;

    private final FieldNamesBuilder fieldNamesBuilder = new FieldNamesBuilder();

    @Override
    public String format(LogRecord record) {
        // jboss will probably swallow any exception anyway, but throwing a runtime exception is better than sending nonsense to redis
        if (configFailed) {
            throw new LoggerInitializationException("Cannot format log record: configuration failed.");
        }

        if (!configured) {
            configure();
        }

        JBossLogEvent logEvent = new JBossLogEvent((ExtLogRecord) record);

        String append = jsonLayout.getString(logEvent);

        return append;
    }

    private synchronized void configure() {
        if (configured) {
            return;
        }

        if (configFailed) {
            throw new LoggerInitializationException("JBossLogstashFormatter configuration failed");
        }

        try {
            jsonLayout = new LogstashJsonFormatter(fieldNamesBuilder.build(), locationAsObject, mdcAsObject, tags, additionalFields, suppressFields, mdcInclude, mdcExclude);
        } catch (RuntimeException e) {
            SimpleLog.warn("An error occurred while configuring the JBossLogstashFormatter. " + ExceptionUtil.getStackTraceAsString(e));
            configFailed = true;

            throw e;
        }

        configured = true;
    }

    // like the setters in JBossRedisHandler, all properties are treated as strings to allow resolution of environment and system
    // properties in jboss' bootstrap logging.properties file

    public void setLocationAsObject(String locationAsObject) {
        this.locationAsObject = Boolean.parseBoolean(JULConfigUtil.resolveSubstitutions(locationAsObject));
    }

    public void setMdcAsObject(String mdcAsObject) {
        this.mdcAsObject = Boolean.parseBoolean(JULConfigUtil.resolveSubstitutions(mdcAsObject));
    }

    public void setAdditionalFieldsJson(String additionalFieldsJson) {
        String resolvedJson = JULConfigUtil.resolveSubstitutions(additionalFieldsJson);
        Map<String, String> additionalFields = GsonUtil.GSON.fromJson(resolvedJson, GsonUtil.MAP_OF_STRING_STRING_TYPE);
        setAdditionalFields(additionalFields);
    }

    public void setAdditionalFields(Map<String, String> additionalFields) {
        this.additionalFields = additionalFields;
    }

    public void setSuppressFields(Set<Field> suppressFields) {
        this.suppressFields = suppressFields;
    }

    public void setSuppressFieldsJson(String suppressFieldsJson) {
        String resolvedJson = JULConfigUtil.resolveSubstitutions(suppressFieldsJson);
        Set<Field> suppressFields = GsonUtil.GSON.fromJson(resolvedJson, GsonUtil.SET_OF_FIELD_TYPE);
        setSuppressFields(suppressFields);
    }

    public void setMdcInclude(Set<String> mdcInclude) {
        this.mdcInclude = mdcInclude;
    }

    public void setMdcIncludeJson(String mdcIncludeJson) {
        String resolvedJson = JULConfigUtil.resolveSubstitutions(mdcIncludeJson);
        Set<String> set = GsonUtil.GSON.fromJson(resolvedJson, GsonUtil.SET_OF_STRING_TYPE);
        setMdcInclude(set);

    }

    public void setMdcExcludeJson(String mdcExcludeJson) {
        String resolvedJson = JULConfigUtil.resolveSubstitutions(mdcExcludeJson);
        Set<String> set = GsonUtil.GSON.fromJson(resolvedJson, GsonUtil.SET_OF_STRING_TYPE);
        setMdcExclude(set);
    }

    public void setMdcExclude(Set<String> mdcExclude) {
        this.mdcExclude = mdcExclude;
    }

    public void setTagsJson(String tagsJson) {
        String resolvedJson = JULConfigUtil.resolveSubstitutions(tagsJson);
        List<String> list = GsonUtil.GSON.fromJson(resolvedJson, GsonUtil.LIST_OF_STRING_TYPE);
        setTags(list);
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setThread(String thread) {
        fieldNamesBuilder.setThread(JULConfigUtil.resolveSubstitutions(thread));
    }

    public void setLevel(String level) {
        fieldNamesBuilder.setLevel(JULConfigUtil.resolveSubstitutions(level));
    }

    public void setMessage(String message) {
        fieldNamesBuilder.setMessage(JULConfigUtil.resolveSubstitutions(message));
    }

    public void setLogger(String logger) {
        fieldNamesBuilder.setLogger(JULConfigUtil.resolveSubstitutions(logger));
    }

    public void setException(String exception) {
        fieldNamesBuilder.setException(JULConfigUtil.resolveSubstitutions(exception));
    }

    public void setLocation(String location) {
        fieldNamesBuilder.setLocation(JULConfigUtil.resolveSubstitutions(location));
    }

    public void setTimestamp(String timestamp) {
        fieldNamesBuilder.setTimestamp(JULConfigUtil.resolveSubstitutions(timestamp));
    }

    public void setMdc(String mdc) {
        fieldNamesBuilder.setMdc(JULConfigUtil.resolveSubstitutions(mdc));
    }

    public void setClassField(String classField) {
        fieldNamesBuilder.setClassField(JULConfigUtil.resolveSubstitutions(classField));
    }

    public void setMethod(String method) {
        fieldNamesBuilder.setMethod(JULConfigUtil.resolveSubstitutions(method));
    }

    public void setFile(String file) {
        fieldNamesBuilder.setFile(JULConfigUtil.resolveSubstitutions(file));
    }

    public void setLine(String line) {
        fieldNamesBuilder.setLine(JULConfigUtil.resolveSubstitutions(line));
    }

    public void setTagsField(String tagsField) {
        fieldNamesBuilder.setTags(JULConfigUtil.resolveSubstitutions(tagsField));
    }
}
