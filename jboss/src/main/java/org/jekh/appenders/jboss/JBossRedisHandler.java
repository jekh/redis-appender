package org.jekh.appenders.jboss;

import org.jekh.appenders.Defaults;
import org.jekh.appenders.client.RedisClient;
import org.jekh.appenders.client.RedisClientBuilder;
import org.jekh.appenders.jul.RedisLoggingHandler;

import java.nio.charset.Charset;
import java.util.logging.LogRecord;

public class JBossRedisHandler extends RedisLoggingHandler {
    private volatile boolean configured = false;

    private String redisKey = Defaults.REDIS_KEY;

    // redis connection settings
    private String host = Defaults.HOST;
    private int port = Defaults.PORT;
    private int timeoutMs = Defaults.TIMEOUT_MS;
    private String password = Defaults.PASSWORD;
    private int database = Defaults.DATABASE;
    private String clientName = Defaults.CLIENT_NAME;
    private boolean tls = Defaults.TLS;
    private boolean synchronous = Defaults.SYNCHRONOUS;
    private int redisPushThreads = Defaults.REDIS_PUSH_THREADS;
    private int maxMessagesPerPush = Defaults.MAX_MESSAGES_PER_PUSH;
    private int logQueueSize = Defaults.LOG_QUEUE_SIZE;
    private Charset charset = Defaults.CHARSET;
    private boolean debug = false;

    public void setRedisKey(String redisKey) {
        this.redisKey = redisKey;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public void setSynchronous(boolean synchronous) {
        this.synchronous = synchronous;
    }

    public void setRedisPushThreads(int redisPushThreads) {
        this.redisPushThreads = redisPushThreads;
    }

    public void setMaxMessagesPerPush(int maxMessagesPerPush) {
        this.maxMessagesPerPush = maxMessagesPerPush;
    }

    public void setLogQueueSize(int logQueueSize) {
        this.logQueueSize = logQueueSize;
    }

    public void setTls(boolean tls) {
        this.tls = tls;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setCharset(String charset) {
        this.charset = Charset.forName(charset);
    }

    public JBossRedisHandler() {
    }

    @Override
    public void publish(LogRecord record) {
        if (!configured) {
            configure();
        }

        super.publish(record);
    }

    private synchronized void configure() {
        if (configured) {
            return;
        }

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
                .debug(debug)
                .build();

        client.start();

        setClient(client);

        if (getFormatter() == null) {
            setFormatter(new JBossLogstashFormatter());
        }

        configured = true;
    }
}
