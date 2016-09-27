package org.jekh.appenders.logback.test;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.gaffer.GafferConfigurator;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import org.jekh.appenders.gson.GsonUtil;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

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
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {

            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            configurator.doConfigure(TestUtil.class.getResourceAsStream(xmlResource));
        } catch (JoranException ignored) {
        }

        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }

    public static void configLoggerFromGroovy(String groovyResource) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        GafferConfigurator configurator = new GafferConfigurator(context);
        context.reset();
        configurator.run(TestUtil.class.getResource(groovyResource));

        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }

    public static void pause(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
