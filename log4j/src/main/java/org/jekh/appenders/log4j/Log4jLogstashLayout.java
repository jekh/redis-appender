package org.jekh.appenders.log4j;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.jekh.appenders.Defaults;
import org.jekh.appenders.Field;
import org.jekh.appenders.GenericLogEvent;
import org.jekh.appenders.LogstashJsonFormatter;
import org.jekh.appenders.gson.GsonUtil;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Plugin(name = "LogstashLayout", category = "Core", elementType = "layout")
public class Log4jLogstashLayout extends AbstractStringLayout {
    private final LogstashJsonFormatter jsonLayout;

    private Log4jLogstashLayout(Charset charset, LogstashJsonFormatter jsonLayout) {
        super(charset);

        this.jsonLayout = jsonLayout;
    }

    @PluginBuilderFactory
    public static Log4jLogstashLayout.Builder newBuilder() {
        return new Log4jLogstashLayout.Builder();
    }

    public static class Builder implements org.apache.logging.log4j.core.util.Builder<Log4jLogstashLayout> {
        @PluginElement("fieldNames")
        private FieldNamesPlugin fieldNamesPlugin = FieldNamesPlugin.newBuilder().build();

        @PluginBuilderAttribute
        private boolean locationAsObject = Defaults.LOCATION_AS_OBJECT;

        @PluginBuilderAttribute
        private boolean mdcAsObject = Defaults.MDC_AS_OBJECT;

        @PluginBuilderAttribute
        private String tagsJson;

        /**
         * The log4j 2 plugin framework doesn't provide a suitable mechanism to specify map and list values in XML, so we have to
         * provide equivalent '-json' parameters, similar to the parameters for other logger frameworks.
         */
        private List<String> tags = Defaults.TAGS;

        @PluginBuilderAttribute
        private String additionalFieldsJson;

        private Map<String, String> additionalFields = Defaults.ADDITIONAL_FIELDS;

        @PluginBuilderAttribute
        private String suppressFieldsJson;

        private Set<Field> suppressFields = Defaults.SUPPRESS_FIELDS;

        @PluginBuilderAttribute
        private String mdcIncludeJson;

        private Set<String> mdcInclude = Defaults.MDC_INCLUDE;

        @PluginBuilderAttribute
        private String mdcExcludeJson;

        private Set<String> mdcExclude = Defaults.MDC_EXCLUDE;

        @PluginBuilderAttribute
        private Charset charset = Defaults.CHARSET;

        private Builder() {
        }

        public Builder withFieldNamesPlugin(FieldNamesPlugin fieldNamesPlugin) {
            this.fieldNamesPlugin = fieldNamesPlugin;
            return this;
        }

        public Builder withLocationAsObject(boolean locationAsObject) {
            this.locationAsObject = locationAsObject;
            return this;
        }

        public Builder withMdcAsObject(boolean mdcAsObject) {
            this.mdcAsObject = mdcAsObject;
            return this;
        }

        public Builder withTagsJson(String tagsJson) {
            this.tagsJson = tagsJson;
            return this;
        }

        public Builder withTags(List<String> tags) {
            this.tags = new ArrayList<>(tags);
            return this;
        }

        public Builder withAdditionalFieldsJson(String additionalFieldsJson) {
            this.additionalFieldsJson = additionalFieldsJson;
            return this;
        }

        public Builder withAdditionalFields(Map<String, String> additionalFields) {
            this.additionalFields = additionalFields;
            return this;
        }

        public Builder withSuppressFieldsJson(String suppressFieldsJson) {
            this.suppressFieldsJson = suppressFieldsJson;
            return this;
        }

        public Builder withSuppressFields(Set<Field> suppressFields) {
            this.suppressFields = suppressFields;
            return this;
        }

        public Builder withMdcIncludeJson(String mdcIncludeJson) {
            this.mdcIncludeJson = mdcIncludeJson;
            return this;
        }

        public Builder withMdcInclude(Set<String> mdcInclude) {
            this.mdcInclude = mdcInclude;
            return this;
        }

        public Builder withMdcExcludeJson(String mdcExcludeJson) {
            this.mdcExcludeJson = mdcExcludeJson;
            return this;

        }

        public Builder withMdcExclude(Set<String> mdcExclude) {
            this.mdcExclude = mdcExclude;
            return this;
        }

        public Builder withCharset(Charset charset) {
            this.charset = charset;
            return this;
        }

        @Override
        public Log4jLogstashLayout build() {
            // parse json fields passed in as strings
            if (tagsJson != null) {
                tags = GsonUtil.GSON.fromJson(tagsJson, GsonUtil.LIST_OF_STRING_TYPE);
            }

            if (additionalFieldsJson != null) {
                additionalFields = GsonUtil.GSON.fromJson(additionalFieldsJson, GsonUtil.MAP_OF_STRING_STRING_TYPE);
            }

            if (suppressFieldsJson != null) {
                suppressFields = GsonUtil.GSON.fromJson(suppressFieldsJson, GsonUtil.SET_OF_FIELD_TYPE);
            }

            if (mdcIncludeJson != null) {
                mdcInclude = GsonUtil.GSON.fromJson(mdcIncludeJson, GsonUtil.SET_OF_STRING_TYPE);
            }

            if (mdcExcludeJson != null) {
                mdcExclude = GsonUtil.GSON.fromJson(mdcExcludeJson, GsonUtil.SET_OF_STRING_TYPE);
            }

            LogstashJsonFormatter logstashJsonFormatter = new LogstashJsonFormatter(fieldNamesPlugin.getFieldNames(), locationAsObject, mdcAsObject, tags, additionalFields, suppressFields, mdcInclude, mdcExclude);

            return new Log4jLogstashLayout(charset, logstashJsonFormatter);
        }
    }

    @Override
    public String toSerializable(LogEvent log4jLogEvent) {
        GenericLogEvent event = new Log4JLogEvent(log4jLogEvent);

        return jsonLayout.getString(event);
    }
}
