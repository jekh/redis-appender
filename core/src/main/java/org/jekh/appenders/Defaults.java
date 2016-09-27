package org.jekh.appenders;

import redis.clients.jedis.Protocol;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Defaults {
    public static final String REDIS_KEY = "logstash";
    public static final String HOST = Protocol.DEFAULT_HOST;
    public static final int PORT = Protocol.DEFAULT_PORT;
    public static final boolean TLS = false;
    public static final int TIMEOUT_MS = Protocol.DEFAULT_TIMEOUT;
    public static final String PASSWORD = null;
    public static final int DATABASE = Protocol.DEFAULT_DATABASE;
    public static final String CLIENT_NAME = null;

    public static final boolean LOCATION_AS_OBJECT = false;

    public static final boolean MDC_AS_OBJECT = false;

    /**
     * Logback-specific field to assist with identification of the calling method.
     */
    public static final int LOCATION_DEPTH = 0;

    /**
     * If tags is null, the field will not be added to the json output. If it is an empty list, an empty list will be added to the json.
     */
    public static final List<String> TAGS = null;

    public static final Map<String, String> ADDITIONAL_FIELDS = Collections.emptyMap();

    public static final EnumSet<Field> SUPPRESS_FIELDS = EnumSet.noneOf(Field.class);

    /**
     * Keys from the MDC to include in the json output. If null, all keys will be included; otherwise, only the key in the list (if any) will be included.
     */
    public static final Set<String> MDC_INCLUDE = null;
    public static final Set<String> MDC_EXCLUDE = Collections.emptySet();

    public static final String THREAD_FIELD = "thread";
    public static final String LEVEL_FIELD = "level";
    public static final String MESSAGE_FIELD = "message";
    public static final String LOGGER_FIELD = "logger";
    public static final String EXCEPTION_FIELD = "exception";
    public static final String LOCATION_FIELD = "location";
    public static final String TIMESTAMP_FIELD = "@timestamp";
    public static final String MDC_FIELD = "mdc";

    // computeLocation data field names
    public static final String CLASS_FIELD = "class";
    public static final String METHOD_FIELD = "method";
    public static final String FILE_FIELD = "file";
    public static final String LINE_FIELD = "line";

    // logstash-specific capabilities
    public static final String TAGS_FIELD = "tags";

    public static final Charset CHARSET = StandardCharsets.UTF_8;

    public static final boolean SYNCHRONOUS = false;
    public static final int LOG_QUEUE_SIZE = 100000;
    public static final int MAX_MESSAGES_PER_PUSH = 100;
    public static final int REDIS_PUSH_THREADS = 5;
    public static final int MAX_THREAD_BLOCK_TIME_MS = Integer.MAX_VALUE;
    public static final int WORKER_TIMEOUT_MS = Integer.MAX_VALUE;

    public static final int INITIAL_SYNC_CLIENT_POOL_SIZE = 5;
    public static final int MAX_SYNC_CLIENT_POOL_SIZE = 25;
}
