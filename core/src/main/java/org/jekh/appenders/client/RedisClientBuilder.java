package org.jekh.appenders.client;

import org.jekh.appenders.Defaults;

import java.nio.charset.Charset;

public class RedisClientBuilder {
    private String redisKey = Defaults.REDIS_KEY;
    private String host = Defaults.HOST;
    private int port = Defaults.PORT;
    private boolean tls = Defaults.TLS;
    private int timeoutMs = Defaults.TIMEOUT_MS;
    private String password = Defaults.PASSWORD;
    private int database = Defaults.DATABASE;
    private String clientName = Defaults.CLIENT_NAME;
    private Charset charset = Defaults.CHARSET;
    private int threads = Defaults.REDIS_PUSH_THREADS;
    private int maxMessagesPerPush = Defaults.MAX_MESSAGES_PER_PUSH;
    private int logQueueSize = Defaults.LOG_QUEUE_SIZE;
    private boolean synchronous = Defaults.SYNCHRONOUS;
    private int maxThreadBlockTimeMs = Defaults.MAX_THREAD_BLOCK_TIME_MS;
    private int workerTimeoutMs = Defaults.WORKER_TIMEOUT_MS;

    private boolean debug = false;

    public static RedisClientBuilder builder() {
        return new RedisClientBuilder();
    }

    public RedisClientBuilder redisKey(String redisKey) {
        this.redisKey = redisKey;
        return this;
    }

    public RedisClientBuilder host(String host) {
        this.host = host;
        return this;
    }

    public RedisClientBuilder port(int port) {
        this.port = port;
        return this;
    }

    public RedisClientBuilder tls(boolean tls) {
        this.tls = tls;
        return this;
    }

    public RedisClientBuilder timeoutMs(int timeout) {
        this.timeoutMs = timeout;
        return this;
    }

    public RedisClientBuilder password(String password) {
        this.password = password;
        return this;
    }

    public RedisClientBuilder database(int database) {
        this.database = database;
        return this;
    }

    public RedisClientBuilder clientName(String clientName) {
        this.clientName = clientName;
        return this;
    }

    public RedisClientBuilder charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public RedisClientBuilder threads(int threads) {
        this.threads = threads;
        return this;
    }

    public RedisClientBuilder maxMessagesPerPush(int maxMessagesPerPush) {
        this.maxMessagesPerPush = maxMessagesPerPush;
        return this;
    }

    public RedisClientBuilder logQueueSize(int logQueueSize) {
        this.logQueueSize = logQueueSize;
        return this;
    }

    public RedisClientBuilder debug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public RedisClientBuilder debug() {
        this.debug = true;
        return this;
    }

    public RedisClientBuilder synchronous(boolean synchronous) {
        this.synchronous = synchronous;
        return this;
    }

    public RedisClientBuilder synchronous() {
        this.synchronous = true;
        return this;
    }

    public RedisClientBuilder async() {
        this.synchronous = false;
        return this;
    }

    public RedisClientBuilder tls() {
        this.tls = true;
        return this;
    }

    public RedisClientBuilder maxThreadBlockTimeMs(int maxThreadBlockTimeMs) {
        if (maxThreadBlockTimeMs < 0) {
            this.maxThreadBlockTimeMs = Integer.MAX_VALUE;
        } else {
            this.maxThreadBlockTimeMs = maxThreadBlockTimeMs;
        }
        return this;
    }

    /**
     * Never block application threads that are logging. If the log queue (see {@link #logQueueSize(int)}) fills up, log messages
     * will be discarded, rather than blocking the application.
     */
    public RedisClientBuilder neverBlockApplicationThreads() {
        this.maxThreadBlockTimeMs = 0;
        return this;
    }

    public RedisClientBuilder workerTimeoutMs(int workerTimeoutMs) {
        if (workerTimeoutMs < 0) {
            this.workerTimeoutMs = Integer.MAX_VALUE;
        } else {
            this.workerTimeoutMs = workerTimeoutMs;
        }
        return this;
    }

    public RedisClient build() {
        if (synchronous) {
            return new JedisClient(redisKey, host, port, tls, timeoutMs, password, database, clientName, charset, maxThreadBlockTimeMs, debug);
        } else {
            return new JedisClient(redisKey, host, port, tls, timeoutMs, password, database, clientName, charset, threads, maxMessagesPerPush, logQueueSize, maxThreadBlockTimeMs, workerTimeoutMs, debug);
        }
    }
}
