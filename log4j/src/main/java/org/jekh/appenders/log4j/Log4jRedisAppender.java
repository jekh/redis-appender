package org.jekh.appenders.log4j;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.jekh.appenders.Defaults;
import org.jekh.appenders.client.RedisClient;
import org.jekh.appenders.client.RedisClientBuilder;

import java.io.Serializable;

@Plugin(name = "Redis", category = Node.CATEGORY, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class Log4jRedisAppender extends AbstractAppender {
    private RedisClient client;

    private final String redisKey;
    private final String host;
    private final int port;
    private final int timeoutMs;
    private final String password;
    private final int database;
    private final String clientName;
    private final boolean tls;
    private final boolean synchronous;
    private final int redisPushThreads;
    private final int maxMessagesPerPush;
    private final int logQueueSize;
    private final boolean debug;

    protected Log4jRedisAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions,
                                 String redisKey, String host, int port, int timeoutMs, String password, int database, String clientName,
                                 boolean tls, boolean synchronous, int redisPushThreads, int maxMessagesPerPush, int logQueueSize, boolean debug) {
        super(name, filter, layout, ignoreExceptions);

        this.redisKey = redisKey;
        this.host = host;
        this.port = port;
        this.timeoutMs = timeoutMs;
        this.password = password;
        this.database = database;
        this.clientName = clientName;
        this.tls = tls;
        this.synchronous = synchronous;
        this.redisPushThreads = redisPushThreads;
        this.maxMessagesPerPush = maxMessagesPerPush;
        this.logQueueSize = logQueueSize;
        this.debug = debug;
    }

    @PluginFactory
    public static Log4jRedisAppender createAppender(@PluginAttribute("name") String name,
                                                    @PluginAttribute("ignoreExceptions") boolean ignoreExceptions,
                                                    @PluginElement("Layout") Layout<? extends Serializable> layout,
                                                    @PluginElement("Filters") Filter filter,
                                                    @PluginAttribute(value = "redisKey", defaultString = Defaults.REDIS_KEY) String redisKey,
                                                    @PluginAttribute(value = "host", defaultString = Defaults.HOST) String host,
                                                    @PluginAttribute(value = "port", defaultInt = Defaults.PORT) int port,
                                                    @PluginAttribute(value = "timeoutMs", defaultInt = Defaults.TIMEOUT_MS) int timeoutMs,
                                                    @PluginAttribute(value = "password") String password,
                                                    @PluginAttribute(value = "database", defaultInt = Defaults.DATABASE) int database,
                                                    @PluginAttribute("clientName") String clientName,
                                                    @PluginAttribute("tls") boolean tls,
                                                    @PluginAttribute("synchronous") boolean synchronous,
                                                    @PluginAttribute(value = "redisPushThreads", defaultInt = Defaults.REDIS_PUSH_THREADS) int redisPushThreads,
                                                    @PluginAttribute(value = "maxMessagesPerPush", defaultInt = Defaults.MAX_MESSAGES_PER_PUSH) int maxMessagesPerPush,
                                                    @PluginAttribute(value = "logQueueSize", defaultInt = Defaults.LOG_QUEUE_SIZE) int logQueueSize,
                                                    @PluginAttribute(value = "debug") boolean debug
    ) {

        if (name == null) {
            LOGGER.error("No name provided for Log4jRedisAppender");
            return null;
        }

        if (layout == null) {
            layout = Log4jLogstashLayout.newBuilder().build();
        }

        return new Log4jRedisAppender(name, filter, layout, ignoreExceptions, redisKey, host, port, timeoutMs, password, database, clientName, tls, synchronous, redisPushThreads, maxMessagesPerPush, logQueueSize, debug);
    }


    @Override
    public void append(LogEvent logEvent) {
        byte[] appendBytes = getLayout().toByteArray(logEvent);

        client.sendToRedis(appendBytes);
    }

    @Override
    public void start() {
        super.start();

        client = RedisClientBuilder.builder()
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
                // for log4j, charset is handled in the layout instead of the appender
                .debug(debug)
                .build();

        client.start();
    }

    @Override
    public void stop() {
        super.stop();
        client.stop();
    }
}
