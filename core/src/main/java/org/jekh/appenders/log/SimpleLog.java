package org.jekh.appenders.log;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Extremely simple mechanism for messages to stdout or stderr, since we don't have access to a robust logging facility.
 */
public class SimpleLog {
    public static void debug(String string) {
        System.out.println(DateTimeFormatter.ISO_INSTANT.format(Instant.now()) + " [DEBUG] [" + Thread.currentThread().getName() + "] " + string);
    }

    public static void warn(String string) {
        System.err.println(DateTimeFormatter.ISO_INSTANT.format(Instant.now()) + " [WARN] [" + Thread.currentThread().getName() + "] " + string);
    }
}
