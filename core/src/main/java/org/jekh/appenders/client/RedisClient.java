package org.jekh.appenders.client;

public interface RedisClient {
    long sendToRedis(String string);

    long sendToRedis(byte[] bytes);

    void start();

    void stop();
}
