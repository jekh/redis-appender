package org.jekh.appenders.jboss;

import org.jekh.appenders.Defaults;
import org.jekh.appenders.client.RedisClient;
import org.jekh.appenders.client.RedisClientBuilder;
import org.jekh.appenders.exception.ExceptionUtil;
import org.jekh.appenders.exception.LoggerInitializationException;
import org.jekh.appenders.jul.RedisLoggingHandler;
import org.jekh.appenders.jul.util.JULConfigUtil;
import org.jekh.appenders.log.SimpleLog;

import java.nio.charset.Charset;
import java.util.logging.LogRecord;

public class JBossRedisHandler extends RedisLoggingHandler {
    private volatile boolean configured = false;

    /**
     * Configuration failed due to an exception.
     */
    private volatile boolean configFailed = false;

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
    private int maxThreadBlockTimeMs = Defaults.MAX_THREAD_BLOCK_TIME_MS;
    private int workerTimeoutMs = Defaults.WORKER_TIMEOUT_MS;

    // JBoss will resolve properties with substitutions (e.g. ${REDIS_HOST}) automatically when they are placed in standalone.xml,
    // but not in the "bootstrap" logger configured in logging.properties. To allow property substitutions, all values are treated as
    // strings and resolved using the JUL substitution resolver.

    public void setRedisKey(String redisKey) {
        this.redisKey = JULConfigUtil.resolveSubstitutions(redisKey);
    }

    public void setHost(String host) {
        this.host = JULConfigUtil.resolveSubstitutions(host);
    }

    public void setPort(String port) {
        this.port = Integer.parseInt(JULConfigUtil.resolveSubstitutions(port));
    }

    public void setTimeoutMs(String timeoutMs) {
        this.timeoutMs = Integer.parseInt(JULConfigUtil.resolveSubstitutions(timeoutMs));
    }

    public void setPassword(String password) {
        this.password = JULConfigUtil.resolveSubstitutions(password);
    }

    public void setDatabase(String database) {
        this.database = Integer.parseInt(JULConfigUtil.resolveSubstitutions(database));
    }

    public void setClientName(String clientName) {
        this.clientName = JULConfigUtil.resolveSubstitutions(clientName);
    }

    public void setSynchronous(String synchronous) {
        this.synchronous = Boolean.parseBoolean(JULConfigUtil.resolveSubstitutions(synchronous));
    }

    public void setRedisPushThreads(String redisPushThreads) {
        this.redisPushThreads = Integer.parseInt(JULConfigUtil.resolveSubstitutions(redisPushThreads));
    }

    public void setMaxMessagesPerPush(String maxMessagesPerPush) {
        this.maxMessagesPerPush = Integer.parseInt(JULConfigUtil.resolveSubstitutions(maxMessagesPerPush));
    }

    public void setLogQueueSize(String logQueueSize) {
        this.logQueueSize = Integer.parseInt(JULConfigUtil.resolveSubstitutions(logQueueSize));
    }

    public void setTls(String tls) {
        this.tls = Boolean.parseBoolean(JULConfigUtil.resolveSubstitutions(tls));
    }

    public void setDebug(String debug) {
        this.debug = Boolean.parseBoolean(JULConfigUtil.resolveSubstitutions(debug));
    }

    public void setCharset(String charset) {
        this.charset = Charset.forName(JULConfigUtil.resolveSubstitutions((charset)));
    }

    public void setMaxThreadBlockTimeMs(String maxThreadBlockTimeMs) {
        this.maxThreadBlockTimeMs = Integer.parseInt(JULConfigUtil.resolveSubstitutions(maxThreadBlockTimeMs));
    }

    public void setWorkerTimeoutMs(String workerTimeoutMs) {
        this.workerTimeoutMs = Integer.parseInt(JULConfigUtil.resolveSubstitutions(workerTimeoutMs));
    }

    public JBossRedisHandler() {
    }

    @Override
    public void publish(LogRecord record) {
        // if configuration was aborted due to an error, don't even try to log anything
        if (configFailed) {
            return;
        }

        if (!configured) {
            configure();
        }

        super.publish(record);
    }

    private synchronized void configure() {
        if (configured) {
            return;
        }

        if (configFailed) {
            throw new LoggerInitializationException("JBossRedisHandler configuration failed");
        }

        RedisClient client;

        // jboss seems to swallow exceptions that occur during configuration, so if any do occur, log them manually
        try {
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
                    .maxThreadBlockTimeMs(maxThreadBlockTimeMs)
                    .workerTimeoutMs(workerTimeoutMs)
                    .debug(debug)
                    .build();

            client.start();
        } catch (RuntimeException e) {
            SimpleLog.warn("An error occurred while configuring the JBossRedisHandler. No logs will be sent to Redis. " + ExceptionUtil.getStackTraceAsString(e));
            configFailed = true;

            throw e;
        }

        setClient(client);

        if (getFormatter() == null) {
            setFormatter(new JBossLogstashFormatter());
        }

        configured = true;
    }

}
