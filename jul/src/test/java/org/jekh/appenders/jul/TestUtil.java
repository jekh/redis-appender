package org.jekh.appenders.jul;

import org.jekh.appenders.gson.GsonUtil;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.LogManager;

public class TestUtil {
    public static String readLastRedisEntry(String key) {
        Jedis jedis = new Jedis();
        String lastEntry = jedis.rpop(key);
        jedis.close();

        return lastEntry;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> jsonToMap(String jsonString) {
        return GsonUtil.GSON.fromJson(jsonString, Map.class);
    }

    public static void clearRedis() {
        Jedis jedis = new Jedis();
        jedis.ltrim("logstash", 1, 0);
        jedis.close();
    }

    public static void configLoggerFromPropertiesFile(String loggerProperties) {
        try (InputStream configFile = TestUtil.class.getResourceAsStream(loggerProperties)) {
            LogManager.getLogManager().readConfiguration(configFile);
        } catch (IOException e) {
            throw new RuntimeException("Could not config logger from properties file: " + loggerProperties, e);
        }
    }

    public static void pause(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
