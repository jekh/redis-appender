package org.jekh.appenders.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;
import org.jekh.appenders.Defaults;
import org.jekh.appenders.Field;
import org.jekh.appenders.LogstashJsonFormatter;
import org.jekh.appenders.gson.GsonUtil;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LogbackLogstashLayout extends LayoutBase<ILoggingEvent> {
    // default formatter
    private LogstashJsonFormatter jsonLayout = LogstashJsonFormatter.DEFAULT_FORMATTER;

    // have to use a special builder, since logback's Joran configuration system will get confused without setters with 'void' return types.
    private LogbackFieldNamesBuilder fieldNames = new LogbackFieldNamesBuilder();

    private boolean locationAsObject = Defaults.LOCATION_AS_OBJECT;

    private boolean mdcAsObject = Defaults.MDC_AS_OBJECT;

    private int locationDepth = Defaults.LOCATION_DEPTH;

    /**
     * If tags is null, the field will not be added to the json output. If it is an empty list, an empty list will be added to the json.
     */
    private List<String> tags = Defaults.TAGS;

    private Map<String, String> additionalFields = Defaults.ADDITIONAL_FIELDS;

    private EnumSet<Field> suppressFields = Defaults.SUPPRESS_FIELDS;

    /**
     * Keys from the MDC to include in the json output. If null, all keys will be included; otherwise, only the key in the list (if any) will be included.
     */
    private Set<String> mdcInclude = Defaults.MDC_INCLUDE;
    private Set<String> mdcExclude = Defaults.MDC_EXCLUDE;

    public void setLocationAsObject(boolean locationAsObject) {
        this.locationAsObject = locationAsObject;
    }

    public void setMdcAsObject(boolean mdcAsObject) {
        this.mdcAsObject = mdcAsObject;
    }

    public void setLocationDepth(int locationDepth) {
        this.locationDepth = locationDepth;
    }

    /**
     * Set additional fields using a JSON string containing key-value pairs.
     * This method can't simply be an overload of {@link #setAdditionalFields(Map)}, since the Joran configurer for logback
     * looks for setters by name, ignoring the parameters, and gets confused when it finds multiple setters.
     *
     * @param additionalFieldsJson additional fields as key-value pairs in a JSON string
     */
    public void setAdditionalFieldsJson(String additionalFieldsJson) {
        Map<String, String> additionalFields = GsonUtil.GSON.fromJson(additionalFieldsJson, GsonUtil.MAP_OF_STRING_STRING_TYPE);
        setAdditionalFields(additionalFields);
    }

    public void setAdditionalFields(Map<String, String> additionalFields) {
        this.additionalFields = additionalFields;
    }

    public void setSuppressFields(Set<Field> suppressFields) {
        this.suppressFields = suppressFields.isEmpty() ? EnumSet.noneOf(Field.class) : EnumSet.copyOf(suppressFields);
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

    @Override
    public String doLayout(ILoggingEvent logbackEvent) {
        LogbackLogEvent event = new LogbackLogEvent(logbackEvent, locationDepth);

        return jsonLayout.getString(event);
    }


    public void setFieldNames(LogbackFieldNamesBuilder fieldNamesBuilder) {
        this.fieldNames = fieldNamesBuilder;
    }

    public LogbackFieldNamesBuilder getFieldNames() {
        return fieldNames;
    }

    public boolean isLocationAsObject() {
        return locationAsObject;
    }

    public boolean isMdcAsObject() {
        return mdcAsObject;
    }

    public int getLocationDepth() {
        return locationDepth;
    }

    public List<String> getTags() {
        return tags;
    }

    public Map<String, String> getAdditionalFields() {
        return additionalFields;
    }

    public EnumSet<Field> getSuppressFields() {
        return suppressFields;
    }

    public Set<String> getMdcInclude() {
        return mdcInclude;
    }

    public Set<String> getMdcExclude() {
        return mdcExclude;
    }

    @Override
    public void start() {
        super.start();
        jsonLayout = new LogstashJsonFormatter(fieldNames.build(), locationAsObject, mdcAsObject, tags, additionalFields, suppressFields, mdcInclude, mdcExclude);
    }
}
