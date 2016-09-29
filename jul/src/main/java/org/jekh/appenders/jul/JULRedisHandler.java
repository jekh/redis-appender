package org.jekh.appenders.jul;

import org.jekh.appenders.Defaults;
import org.jekh.appenders.client.RedisClient;
import org.jekh.appenders.client.RedisClientBuilder;
import org.jekh.appenders.exception.LoggerInitializationException;
import org.jekh.appenders.jul.util.JULConfigUtil;

import java.nio.charset.Charset;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class JULRedisHandler extends RedisLoggingHandler {
    private static final String HANDLER_PROPERTY_BASE = JULRedisHandler.class.getCanonicalName();

    private static final String REDIS_KEY_PROPERTY = HANDLER_PROPERTY_BASE + ".redisKey";
    private static final String HOST_PROPERTY = HANDLER_PROPERTY_BASE + ".host";
    private static final String PORT_PROPERTY = HANDLER_PROPERTY_BASE + ".port";
    private static final String TIMEOUT_PROPERTY = HANDLER_PROPERTY_BASE + ".timeout";
    private static final String PASSWORD_PROPERTY = HANDLER_PROPERTY_BASE + ".password";
    private static final String DATABASE_PROPERTY = HANDLER_PROPERTY_BASE + ".database";
    private static final String CLIENT_NAME_PROPERTY = HANDLER_PROPERTY_BASE + ".clientName";
    private static final String TLS_PROPERTY = HANDLER_PROPERTY_BASE + ".tls";
    private static final String SYNCHRONOUS_PROPERTY = HANDLER_PROPERTY_BASE + ".synchronous";
    private static final String REDIS_PUSH_THREADS_PROPERTY = HANDLER_PROPERTY_BASE + ".redisPushThreads";
    private static final String MAX_MESSAGES_PER_PUSH_PROPERTY = HANDLER_PROPERTY_BASE + ".maxMessagesPerPush";
    private static final String LOG_QUEUE_SIZE_PROPERTY = HANDLER_PROPERTY_BASE + ".logQueueSize";
    private static final String CHARSET_PROPERTY = HANDLER_PROPERTY_BASE + ".charset";
    private static final String MAX_THREAD_BLOCK_TIME_PROPERTY = HANDLER_PROPERTY_BASE + ".maxThreadBlockTimeMs";
    private static final String WORKER_TIMEOUT_PROPERTY = HANDLER_PROPERTY_BASE + ".workerTimeoutMs";
    private static final String DEBUG_PROPERTY = HANDLER_PROPERTY_BASE + ".debug";

    private static final String FORMATTER_PROPERTY = HANDLER_PROPERTY_BASE + ".formatter";

    public JULRedisHandler() {
        configure();
    }

    @Override
    public void publish(LogRecord record) {
        super.publish(record);
    }

    private void configure() {
        // read settings from the logging.properties file

        String redisKey = JULConfigUtil.getProperty(REDIS_KEY_PROPERTY, Defaults.REDIS_KEY);

        String host = JULConfigUtil.getProperty(HOST_PROPERTY, Defaults.HOST);
        int port = JULConfigUtil.getIntProperty(PORT_PROPERTY, Defaults.PORT);
        int timeoutMs = JULConfigUtil.getIntProperty(TIMEOUT_PROPERTY, Defaults.TIMEOUT_MS);
        String password = JULConfigUtil.getProperty(PASSWORD_PROPERTY, Defaults.PASSWORD);
        int database = JULConfigUtil.getIntProperty(DATABASE_PROPERTY, Defaults.DATABASE);
        String clientName = JULConfigUtil.getProperty(CLIENT_NAME_PROPERTY, Defaults.CLIENT_NAME);
        boolean tls = JULConfigUtil.getBooleanProperty(TLS_PROPERTY, Defaults.TLS);
        boolean synchronous = JULConfigUtil.getBooleanProperty(SYNCHRONOUS_PROPERTY, Defaults.SYNCHRONOUS);
        int redisPushThreads = JULConfigUtil.getIntProperty(REDIS_PUSH_THREADS_PROPERTY, Defaults.REDIS_PUSH_THREADS);
        int maxMessagesPerPush = JULConfigUtil.getIntProperty(MAX_MESSAGES_PER_PUSH_PROPERTY, Defaults.MAX_MESSAGES_PER_PUSH);
        int logQueueSize = JULConfigUtil.getIntProperty(LOG_QUEUE_SIZE_PROPERTY, Defaults.LOG_QUEUE_SIZE);
        boolean debug = JULConfigUtil.getBooleanProperty(DEBUG_PROPERTY, false);
        int maxThreadBlockTimeMs = JULConfigUtil.getIntProperty(MAX_THREAD_BLOCK_TIME_PROPERTY, Defaults.MAX_THREAD_BLOCK_TIME_MS);
        int workerTimeoutMs = JULConfigUtil.getIntProperty(WORKER_TIMEOUT_PROPERTY, Defaults.WORKER_TIMEOUT_MS);

        Charset charset = Defaults.CHARSET;
        String charsetString = JULConfigUtil.getProperty(CHARSET_PROPERTY, null);
        if (charsetString != null) {
            charset = Charset.forName(charsetString);
        }

        Formatter formatter;
        String formatterClass = JULConfigUtil.getProperty(FORMATTER_PROPERTY, null);
        // allow instantiating a different formatter
        if (formatterClass != null && !formatterClass.equals(JULLogstashFormatter.class.getCanonicalName())) {
            try {
                Class<?> formatterClazz = Class.forName(formatterClass);
                formatter = (Formatter) formatterClazz.newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new LoggerInitializationException("Could not initialize formatter class: " + formatterClass, e);
            }
        } else {
            formatter = new JULLogstashFormatter();
        }

        this.setFormatter(formatter);

        RedisClient client = RedisClientBuilder.builder()
                .redisKey(redisKey)
                .host(host)
                .port(port)
                .timeoutMs(timeoutMs)
                .password(password)
                .database(database)
                .clientName(clientName)
                .tls(tls)
                .threads(redisPushThreads)
                .logQueueSize(logQueueSize)
                .maxMessagesPerPush(maxMessagesPerPush)
                .synchronous(synchronous)
                .charset(charset)
                .maxThreadBlockTimeMs(maxThreadBlockTimeMs)
                .workerTimeoutMs(workerTimeoutMs)
                .debug(debug)
                .build();

        client.start();

        setClient(client);
    }

}
