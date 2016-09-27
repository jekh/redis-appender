package org.jekh.appenders.log4j.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.jekh.appenders.gson.GsonUtil;
import redis.clients.jedis.Jedis;

import java.net.URISyntaxException;
import java.util.Map;

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

    public static void configLoggerFromXml(String xmlResource) {
        LoggerContext context = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);

        try {
            context.setConfigLocation(TestUtil.class.getResource(xmlResource).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
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
