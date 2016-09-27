package org.jekh.appenders;

import org.jekh.appenders.gson.GsonUtil;
import org.jekh.appenders.mdc.MdcUtil;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LogstashJsonFormatter {
    /**
     * A default formatter, for use when no layout/formatter is specified.
     */
    public static final LogstashJsonFormatter DEFAULT_FORMATTER = new LogstashJsonFormatter(
            new FieldNamesBuilder().build(),
            Defaults.LOCATION_AS_OBJECT,
            Defaults.MDC_AS_OBJECT,
            Defaults.TAGS,
            Defaults.ADDITIONAL_FIELDS,
            Defaults.SUPPRESS_FIELDS,
            Defaults.MDC_INCLUDE,
            Defaults.MDC_EXCLUDE);

    private final FieldNames fieldNames;

    private final boolean locationAsObject;

    private final boolean mdcAsObject;

    /**
     * If tags is null, the field will not be added to the json output. If it is an empty list, an empty list will be added to the json.
     */
    private final List<String> tags;

    private final Map<String, String> additionalFields;

    private final EnumSet<Field> suppressFields;

    /**
     * Keys from the MDC to include in the json output. If null, all keys will be included; otherwise, only the key in the list (if any) will be included.
     */
    private final Set<String> mdcInclude;
    private final Set<String> mdcExclude;

    public LogstashJsonFormatter(FieldNames fieldNames, boolean locationAsObject, boolean mdcAsObject, List<String> tags, Map<String, String> additionalFields, Set<Field> suppressFields, Set<String> mdcInclude, Set<String> mdcExclude) {
        this.fieldNames = fieldNames;
        this.locationAsObject = locationAsObject;
        this.mdcAsObject = mdcAsObject;

        if (tags == null) {
            this.tags = null;
        } else if (tags.isEmpty()) {
            this.tags = Collections.emptyList();
        } else {
            this.tags = new ArrayList<>(tags);
        }

        this.additionalFields = additionalFields != null && !additionalFields.isEmpty() ? new HashMap<>(additionalFields) : Collections.emptyMap();

        this.suppressFields = suppressFields != null && !suppressFields.isEmpty() ? EnumSet.copyOf(suppressFields) : EnumSet.noneOf(Field.class);

        this.mdcInclude = mdcInclude != null ? new HashSet<>(mdcInclude) : null;

        this.mdcExclude = mdcExclude != null && !mdcExclude.isEmpty() ? new HashSet<>(mdcExclude) : Collections.emptySet();
    }


    public String getString(GenericLogEvent event) {
        Map<String, String> mdc = event.getMdc();

        // # of regular fields + number of additional fields + number of MDC fields
        int expectedMapSize;
        if (suppressFields.contains(Field.mdc)) {
            expectedMapSize = Field.values().length + additionalFields.size();
        } else {
            expectedMapSize = Field.values().length + additionalFields.size() + mdc.size();
        }

        // allocating twice as much space as needed to reduce the number of collisions
        // TODO: evaluate performance impact of double-sizing. it may be better simply to keep the "right" size, since we iterate through the entire map anyway.
        Map<String, Object> logMessage = new HashMap<>(expectedMapSize * 2, 1);

        logMessage.put(fieldNames.getMessage(), event.getFormattedMessage());

        logMessage.put(fieldNames.getTimestamp(), DateTimeFormatter.ISO_INSTANT.format(event.getTimestamp()));

        logMessage.put(fieldNames.getLogger(), event.getLoggerName());
        logMessage.put(fieldNames.getLevel(), event.getLevelAsString());
        logMessage.put(fieldNames.getThread(), event.getThreadName());

        if (!suppressFields.contains(Field.exception)) {
            String exceptionAsString = event.getExceptionAsString();
            if (exceptionAsString != null) {
                logMessage.put(fieldNames.getException(), exceptionAsString);
            }
        }

        if (!suppressFields.contains(Field.location)) {
            Map<String, Object> location;
            if (locationAsObject) {
                location = new HashMap<>(4, 1); // the number of location fields is 4, so collisions should not have a major impact on performance
                logMessage.put(fieldNames.getLocation(), location);
            } else {
                location = logMessage;
            }

            StackTraceElement callerLocationFrame = event.getSource();

            if (callerLocationFrame != null) {
                location.put(fieldNames.getClassField(), callerLocationFrame.getClassName());
                location.put(fieldNames.getMethod(), callerLocationFrame.getMethodName());
                location.put(fieldNames.getFile(), callerLocationFrame.getFileName());
                location.put(fieldNames.getLine(), callerLocationFrame.getLineNumber());
            }
        }

        if (!suppressFields.contains(Field.mdc)) {
            Map<String, Object> mdcLog;
            if (mdcAsObject) {
                // TODO: evaluate performance impact of double-sizing. it may be better simply to keep the "right" size, since we iterate through the entire map anyway.
                mdcLog = new HashMap<>(mdc.size() * 2, 1);
                logMessage.put(fieldNames.getMdc(), mdcLog);
            } else {
                mdcLog = logMessage;
            }

            if (mdcInclude == null && mdcExclude.isEmpty()) {
                // fastest and easiest way to add the entire mdc, and likely to be one of the most common use cases
                mdcLog.putAll(mdc);
            } else {
                Set<String> fieldsToInclude = mdcInclude == null ? mdc.keySet() : mdcInclude;

                for (String mdcField : fieldsToInclude) {
                    if (!mdcExclude.contains(mdcField)) {
                        String mdcValue = mdc.get(mdcField);
                        if (mdcValue != null) {
                            mdcLog.put(mdcField, mdcValue);
                        }
                    }
                }
            }
        }

        Map<String, String> fieldsToAppend = additionalFields.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> MdcUtil.resolveSubstitutions(entry.getValue(), mdc)
        ));

        logMessage.putAll(fieldsToAppend);

        // add logstash "tags" field, if present
        if (tags != null) {
            List<String> resolvedTags = tags.stream().map(tag -> MdcUtil.resolveSubstitutions(tag, mdc)).collect(Collectors.toList());
            logMessage.put(fieldNames.getTags(), resolvedTags);
        }

        return GsonUtil.GSON.toJson(logMessage);
    }
}
