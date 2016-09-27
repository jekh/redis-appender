package org.jekh.appenders.jul.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Provides support for the MDC when using java.util.logging with slf4j.
 */
public class JULMdcHelper {
    private static final boolean MDC_AVAILABLE;

    private static final Class<?> SLF4J_MDC_CLASS;
    private static final Method MDC_GET_COPY_OF_CONTEXT_MAP_METHOD;

    static {
        // attempt to load the slf4j MDC class. if it cannot be loaded, the MDC will not be available.

        Class<?> mdcClass;
        try {
            mdcClass = Class.forName("org.slf4j.MDC");
        } catch (ClassNotFoundException e) {
            mdcClass = null;
        }

        SLF4J_MDC_CLASS = mdcClass;

        Method getMdcMethod = null;
        if (SLF4J_MDC_CLASS != null) {
            try {
                getMdcMethod = SLF4J_MDC_CLASS.getMethod("getCopyOfContextMap");
            } catch (NoSuchMethodException e) {
                // can't find the method, even though we found the class. this is unlikely to happen unless slf4j is significantly
                // refactored in an incompatible way. even so, it still means we cannot access the slf4j MDC.
            }
        }

        MDC_GET_COPY_OF_CONTEXT_MAP_METHOD = getMdcMethod;

        if (SLF4J_MDC_CLASS != null && MDC_GET_COPY_OF_CONTEXT_MAP_METHOD != null) {
            MDC_AVAILABLE = true;
        } else {
            MDC_AVAILABLE = false;
        }
    }

    @SuppressWarnings("unchecked") // cast from Object returned by reflection to Map<String, String>
    public static Map<String, String> getMdc() {
        if (!MDC_AVAILABLE) {
            return null;
        }

        try {
            Object mdc = MDC_GET_COPY_OF_CONTEXT_MAP_METHOD.invoke(null);

            return (Map<String, String>) mdc;
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }
}
