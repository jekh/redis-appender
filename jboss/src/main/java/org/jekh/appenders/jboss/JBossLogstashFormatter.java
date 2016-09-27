package org.jekh.appenders.jboss;

import org.jboss.logmanager.ExtLogRecord;
import org.jekh.appenders.Defaults;
import org.jekh.appenders.Field;
import org.jekh.appenders.FieldNamesBuilder;
import org.jekh.appenders.LogstashJsonFormatter;
import org.jekh.appenders.gson.GsonUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.LogRecord;

public class JBossLogstashFormatter extends java.util.logging.Formatter {
    private LogstashJsonFormatter jsonLayout;

    private volatile boolean configured = false;

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

        jsonLayout = new LogstashJsonFormatter(fieldNamesBuilder.build(), locationAsObject, mdcAsObject, tags, additionalFields, suppressFields, mdcInclude, mdcExclude);

        configured = true;
    }

    public void setLocationAsObject(boolean locationAsObject) {
        this.locationAsObject = locationAsObject;
    }

    public void setMdcAsObject(boolean mdcAsObject) {
        this.mdcAsObject = mdcAsObject;
    }

    public void setAdditionalFieldsJson(String additionalFieldsJson) {
        Map<String, String> additionalFields = GsonUtil.GSON.fromJson(additionalFieldsJson, GsonUtil.MAP_OF_STRING_STRING_TYPE);
        setAdditionalFields(additionalFields);
    }

    public void setAdditionalFields(Map<String, String> additionalFields) {
        this.additionalFields = additionalFields;
    }

    public void setSuppressFields(Set<Field> suppressFields) {
        this.suppressFields = suppressFields;
    }

    public void setSuppressFieldsJson(String suppressFieldsJson) {
        Set<Field> suppressFields = GsonUtil.GSON.fromJson(suppressFieldsJson, GsonUtil.SET_OF_FIELD_TYPE);
        setSuppressFields(suppressFields);
    }

    public void setMdcInclude(Set<String> mdcInclude) {
        this.mdcInclude = mdcInclude;
    }

    public void setMdcIncludeJson(String mdcIncludeJson) {
        Set<String> set = GsonUtil.GSON.fromJson(mdcIncludeJson, GsonUtil.SET_OF_STRING_TYPE);
        setMdcInclude(set);

    }

    public void setMdcExcludeJson(String mdcExcludeJson) {
        Set<String> set = GsonUtil.GSON.fromJson(mdcExcludeJson, GsonUtil.SET_OF_STRING_TYPE);
        setMdcExclude(set);
    }

    public void setMdcExclude(Set<String> mdcExclude) {
        this.mdcExclude = mdcExclude;
    }

    public void setTagsJson(String tagsJson) {
        List<String> list = GsonUtil.GSON.fromJson(tagsJson, GsonUtil.LIST_OF_STRING_TYPE);
        setTags(list);
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

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

    public void setTagsField(String tagsField) {
        fieldNamesBuilder.setTags(tagsField);
    }
}
