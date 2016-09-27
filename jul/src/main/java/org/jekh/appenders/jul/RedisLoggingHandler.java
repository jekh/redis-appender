package org.jekh.appenders.jul;

import org.jekh.appenders.client.RedisClient;
import org.jekh.appenders.exception.LoggerInitializationException;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public abstract class RedisLoggingHandler extends java.util.logging.Handler {
    private volatile RedisClient client;

    public RedisLoggingHandler() {
    }

    protected RedisClient getClient() {
        return client;
    }

    protected void setClient(RedisClient client) {
        this.client = client;
    }

    @Override
    public void publish(LogRecord record) {
        if (client == null) {
            throw new LoggerInitializationException("Redis client must be initialized before publishing events to redis");
        }

        Formatter formatter = getFormatter();
        if (formatter == null) {
            throw new LoggerInitializationException("Formatter must be initialized before publishing events to redis");
        }

        String append = formatter.format(record);

        client.sendToRedis(append);
    }


    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
        if (client != null) {
            client.stop();
        }
    }

}
