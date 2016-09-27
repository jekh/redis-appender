package org.jekh.appenders.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.jekh.appenders.Defaults;
import org.jekh.appenders.client.RedisClient;
import org.jekh.appenders.client.RedisClientBuilder;

import java.nio.charset.Charset;

public class LogbackRedisAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    private RedisClient client;

    private Layout<ILoggingEvent> layout = new LogbackLogstashLayout();

    public Layout<ILoggingEvent> getLayout() {
        return layout;
    }

    public void setLayout(Layout<ILoggingEvent> layout) {
        this.layout = layout;
    }

    private String redisKey = Defaults.REDIS_KEY;

    // redis connection settings
    private String host = Defaults.HOST;
    private int port = Defaults.PORT;
    private int timeoutMs = Defaults.TIMEOUT_MS;
    private String password = Defaults.PASSWORD;
    private int database = Defaults.DATABASE;
    private String clientName = Defaults.CLIENT_NAME;
    private boolean tls = Defaults.TLS;

    // RedisClient options
    private boolean synchronous = Defaults.SYNCHRONOUS;
    private int redisPushThreads = Defaults.REDIS_PUSH_THREADS;
    private int maxMessagesPerPush = Defaults.MAX_MESSAGES_PER_PUSH;
    private int logQueueSize = Defaults.LOG_QUEUE_SIZE;

    private Charset charset = Defaults.CHARSET;

    private boolean debug = false;

    public String getRedisKey() {
        return redisKey;
    }

    public void setRedisKey(String redisKey) {
        this.redisKey = redisKey;
    }

    public RedisClient getClient() {
        return client;
    }

    public void setClient(RedisClient client) {
        this.client = client;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public void setSynchronous(boolean synchronous) {
        this.synchronous = synchronous;
    }

    public boolean isSynchronous() {
        return synchronous;
    }

    public int getRedisPushThreads() {
        return redisPushThreads;
    }

    public void setRedisPushThreads(int redisPushThreads) {
        this.redisPushThreads = redisPushThreads;
    }

    public int getMaxMessagesPerPush() {
        return maxMessagesPerPush;
    }

    public void setMaxMessagesPerPush(int maxMessagesPerPush) {
        this.maxMessagesPerPush = maxMessagesPerPush;
    }

    public int getLogQueueSize() {
        return logQueueSize;
    }

    public void setLogQueueSize(int logQueueSize) {
        this.logQueueSize = logQueueSize;
    }

    public boolean isTls() {
        return tls;
    }

    public void setTls(boolean tls) {
        this.tls = tls;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = Charset.forName(charset);
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Work-around when setting debug to true from a groovy configuration file.
     */
    public void setDebugAppender(boolean debug) {
        this.debug = debug;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        String append = layout.doLayout(eventObject);

        client.sendToRedis(append);
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
                .charset(charset)
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
