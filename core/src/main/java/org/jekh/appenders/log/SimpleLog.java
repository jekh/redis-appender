package org.jekh.appenders.log;

/**
 * Extremely simple mechanism for messages to stdout or stderr, since we don't have access to a robust logging facility.
 */
public class SimpleLog {
    public static void debug(String string) {
        System.out.println("[DEBUG] [" + Thread.currentThread().getName() + "] " + string);
    }

    public static void warn(String string) {
        System.err.println("[WARN] [" + Thread.currentThread().getName() + "] " + string);
    }
}
