package org.jekh.appenders.jul.util;

import java.util.logging.LogManager;

/**
 * Utility class for configuring redis-appender-jul using a logging.properties file.
 */
public class JULConfigUtil {
    public static String getProperty(String property, String defaultValue) {
        LogManager manager = LogManager.getLogManager();

        String propertyValue = manager.getProperty(property);
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
}
