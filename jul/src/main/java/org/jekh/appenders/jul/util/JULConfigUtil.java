package org.jekh.appenders.jul.util;

import java.util.logging.LogManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for configuring redis-appender-jul using a logging.properties file.
 */
public class JULConfigUtil {
    /**
     * Regex to match property substitutions in values in logger.properties files. For example:
     * org.jekh.appenders.jul.JULRedisHandler.port=${REDIS_PORT} #resolves REDIS_PORT as a JVM system property
     * org.jekh.appenders.jul.JULRedisHandler.port=${env.REDIS_PORT:6379} #resolves REDIS_PORT as an environment variable with a default value of 6379
     */
    private static final Pattern PROPERTY_SUBSTITUTION_PATTERN = Pattern.compile("\\$\\{([^}:]+)(?::([^}]+))?}");
    private static final int PROPERTY_NAME_GROUP = 1;
    private static final int DEFAULT_VALUE_GROUP = 2;

    public static String getProperty(String property, String defaultValue) {
        LogManager manager = LogManager.getLogManager();

        String propertyValue = resolveSubstitutions(manager.getProperty(property));
        if (propertyValue == null) {
            return defaultValue;
        } else {
            return propertyValue;
        }
    }

    public static boolean getBooleanProperty(String property, boolean defaultValue) {
        String value = getProperty(property, null);
        if (value != null) {
            return Boolean.parseBoolean(value);
        } else {
            return defaultValue;
        }
    }

    public static int getIntProperty(String property, int defaultValue) {
        String value = getProperty(property, null);
        if (value != null) {
            return Integer.parseInt(value);
        } else {
            return defaultValue;
        }
    }

    /**
     * Performs property substitution. This is very similar in principle to the MDC substitution made
     * in {@link org.jekh.appenders.mdc.MdcUtil}, but this resolves system properties and environment variables.
     */
    public static String resolveSubstitutions(String literalValue) {
        if (literalValue == null || !literalValue.contains("${")) {
            return literalValue;
        }

        StringBuffer sb = new StringBuffer(literalValue.length());

        Matcher matcher = PROPERTY_SUBSTITUTION_PATTERN.matcher(literalValue);
        while (matcher.find()) {
            String variableName = matcher.group(PROPERTY_NAME_GROUP);
            // first resolve the variable as a system property, then if that's not found, as an environment property
            String substitution = System.getProperty(variableName);
            if (substitution == null) {
                substitution = System.getenv(variableName);
            }

            // if no system or environment variable were found, use the default value if present
            if (substitution == null) {
                String defaultValue = matcher.group(DEFAULT_VALUE_GROUP);
                if (defaultValue == null) {
                    substitution = "${" + variableName + '}';
                } else {
                    substitution = defaultValue;
                }
            }

            matcher.appendReplacement(sb, Matcher.quoteReplacement(substitution));
        }

        matcher.appendTail(sb);

        return sb.toString();

    }
}
