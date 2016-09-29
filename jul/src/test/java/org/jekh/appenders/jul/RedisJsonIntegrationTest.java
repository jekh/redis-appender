package org.jekh.appenders.jul;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class RedisJsonIntegrationTest {
    @Before
    public void initRedis() {
        TestUtil.clearRedis();
    }

    @SuppressWarnings("unchecked") // hasEntry map matcher needs unchecked cast to Matcher
    @Test
    public void testSetAllSettingsInJULProperties() {
        // basic test that all settings from the config are read and applied correctly
        TestUtil.configLoggerFromPropertiesFile("/jul-allsettings.properties");

        Logger logger = Logger.getLogger("testSetAllSettingsInJULProperties");

        MDC.put("includedkey", "includedvalue");
        MDC.put("excludedkey", "excludedvalue");

        logger.log(Level.FINE, "A simple test message", new RuntimeException("Test exception"));

        // pause to allow the message to be sent to redis
        TestUtil.pause(200);

        String lastEntry = TestUtil.readLastRedisEntry("logstash");
        assertThat("Could not read entry from redis", lastEntry, not(emptyOrNullString()));

        Map<String, Object> loggedMessage = TestUtil.jsonToMap(lastEntry);

        assertThat(loggedMessage, hasEntry("source", "somesource"));
        assertThat(loggedMessage, hasEntry("path", "somepath"));
        assertThat(loggedMessage, hasEntry("type", "sometype"));
        assertThat(loggedMessage, hasEntry("host", "somehost"));
        assertThat(loggedMessage, hasEntry("otherkey", "othervalue"));
        assertThat(loggedMessage, hasEntry("frommdc", "MDC included value is includedvalue and MDC excluded value is excludedvalue"));
        assertThat(loggedMessage, hasEntry("defaultValue", "Default value"));
        assertThat(loggedMessage, hasEntry("noDefaultValue", "No default @{value}"));

        assertThat(loggedMessage, hasEntry("theThread", "1")); // JUL only exposes the thread ID, not the thread name
        assertThat(loggedMessage, hasEntry("severity", "FINE"));
        assertThat(loggedMessage, hasKey("logMessage"));
        assertThat(loggedMessage, hasEntry("loggerName", "testSetAllSettingsInJULProperties"));
        assertThat(loggedMessage, hasKey("throwable"));
        assertThat(loggedMessage, hasKey("logLocation"));

        Map<?, ?> location = (Map<?, ?>) loggedMessage.get("logLocation");
        assertThat(location, hasKey("className"));
        assertThat(location, hasKey("function"));
//        assertThat(location, hasKey("javafile")); // JUL doesn't expose access to the file
        assertThat(location, hasKey("linenumber"));

        assertThat(loggedMessage, hasKey("time"));

        // make sure mdc fields are logged as well
        assertThat(loggedMessage, hasKey("mdcProperties"));
        Map<?, ?> mdc = (Map<?, ?>) loggedMessage.get("mdcProperties");
        assertThat(mdc, hasEntry("includedkey", "includedvalue"));
        assertThat(mdc, not(hasKey("excludedkey")));

        // have to de-generify Matcher here because otherwise assertThat will expect Map<String, ? extends Iterable>, but we can only supply Map<String, Object>
        assertThat(loggedMessage, hasEntry(equalTo("flags"), (Matcher) contains("one", "2", "three")));
    }

    @Test
    public void testDefaultJULProperties() {
        TestUtil.configLoggerFromPropertiesFile("/jul-defaults.properties");

        Logger logger = Logger.getLogger("testDefaultJULProperties");

        MDC.put("keyOne", "valueOne");
        MDC.put("keyTwo", "valueTwo");

        logger.log(Level.FINE, "A simple test message", new RuntimeException("Test exception"));

        // pause to allow the message to be sent to redis
        TestUtil.pause(200);

        String lastEntry = TestUtil.readLastRedisEntry("logstash");
        assertThat("Could not read entry from redis", lastEntry, not(emptyOrNullString()));

        Map<String, Object> loggedMessage = TestUtil.jsonToMap(lastEntry);

        assertThat(loggedMessage, hasEntry("thread", "1")); // JUL only exposes the thread ID, not the thread name
        assertThat(loggedMessage, hasEntry("level", "FINE"));
        assertThat(loggedMessage, hasKey("message"));
        assertThat(loggedMessage, hasEntry("logger", "testDefaultJULProperties"));
        assertThat(loggedMessage, hasKey("exception"));

        // location is not in an object by default
        assertThat(loggedMessage, not(hasKey("location")));
        assertThat(loggedMessage, hasKey("class"));
        assertThat(loggedMessage, hasKey("method"));
//        assertThat(loggedMessage, hasKey("file")); // JUL doesn't expose access to the file
        assertThat(loggedMessage, hasKey("line"));

        assertThat(loggedMessage, hasKey("@timestamp"));

        // make sure mdc fields are logged as well. mdc is not in an object by default.
        assertThat(loggedMessage, not(hasKey("mdc")));
        assertThat(loggedMessage, hasEntry("keyOne", "valueOne"));
        assertThat(loggedMessage, hasEntry("keyTwo", "valueTwo"));

        // have to de-generify Matcher here because otherwise assertThat will expect Map<String, ? extends Iterable>, but we can only supply Map<String, Object>
        assertThat(loggedMessage, not(hasKey("tags")));
    }

    @Test
    public void testSuppressFieldsInJULProperties() {
        TestUtil.configLoggerFromPropertiesFile("/jul-suppressfields.properties");

        Logger logger = Logger.getLogger("testSuppressFieldsInJULProperties");

        MDC.put("keyOne", "valueOne");
        MDC.put("keyTwo", "valueTwo");

        logger.log(Level.FINE, "A simple test message", new RuntimeException("Test exception"));

        // pause to allow the message to be sent to redis
        TestUtil.pause(200);

        String lastEntry = TestUtil.readLastRedisEntry("logstash");
        assertThat("Could not read entry from redis", lastEntry, not(emptyOrNullString()));

        Map<String, Object> loggedMessage = TestUtil.jsonToMap(lastEntry);

        assertThat(loggedMessage, hasEntry("thread", "1"));
        assertThat(loggedMessage, hasEntry("level", "FINE"));
        assertThat(loggedMessage, hasKey("message"));
        assertThat(loggedMessage, hasEntry("logger", "testSuppressFieldsInJULProperties"));

        // exception, location, and mdc are all suppressed
        assertThat(loggedMessage, not(hasKey("exception")));
        assertThat(loggedMessage, not(hasKey("location")));
        assertThat(loggedMessage, not(hasKey("class")));
        assertThat(loggedMessage, not(hasKey("method")));
        assertThat(loggedMessage, not(hasKey("file")));
        assertThat(loggedMessage, not(hasKey("line")));
        assertThat(loggedMessage, not(hasKey("mdc")));
        assertThat(loggedMessage, not(hasEntry("keyOne", "valueOne")));
        assertThat(loggedMessage, not(hasEntry("keyTwo", "valueTwo")));

        // even though mdc was suppressed, there's an additional field with an MDC value that should be present
        assertThat(loggedMessage, hasEntry("additionalKeyOne", "valueOne"));

        assertThat(loggedMessage, hasKey("@timestamp"));

        // have to de-generify Matcher here because otherwise assertThat will expect Map<String, ? extends Iterable>, but we can only supply Map<String, Object>
        assertThat(loggedMessage, not(hasKey("tags")));
    }

    @Test
    public void testJULWithProperties() {
        System.setProperty("SYSTEM_PROPERTY_1", "value from system property");

        TestUtil.configLoggerFromPropertiesFile("/jul-properties.properties");

        Logger logger = Logger.getLogger("testJULWithProperties");

        logger.log(Level.FINE, "A simple test message", new RuntimeException("Test exception"));

        // pause to allow the message to be sent to redis
        TestUtil.pause(200);

        String lastEntry = TestUtil.readLastRedisEntry("logstash");
        assertThat("Could not read entry from redis", lastEntry, not(emptyOrNullString()));

        Map<String, Object> loggedMessage = TestUtil.jsonToMap(lastEntry);

        assertThat(loggedMessage, hasEntry("thread", "1"));
        assertThat(loggedMessage, hasEntry("level", "FINE"));
        assertThat(loggedMessage, hasKey("message"));
        assertThat(loggedMessage, hasEntry("logger", "testJULWithProperties"));
        assertThat(loggedMessage, hasKey("exception"));

        // location is not in an object by default
        assertThat(loggedMessage, not(hasKey("location")));
        assertThat(loggedMessage, hasKey("class"));
        assertThat(loggedMessage, hasKey("method"));
//        assertThat(loggedMessage, hasKey("file"));
        assertThat(loggedMessage, hasKey("line"));

        assertThat(loggedMessage, hasKey("@timestamp"));

        // make sure mdc fields are logged as well. mdc is not in an object by default.
        assertThat(loggedMessage, not(hasKey("mdc")));
        assertThat(loggedMessage, hasEntry("envProperty", "default environment property value"));
        assertThat(loggedMessage, hasEntry("sysProperty", "value from system property"));
        assertThat(loggedMessage, hasEntry("sysPropertyDefault", "default system property value"));

        // have to de-generify Matcher here because otherwise assertThat will expect Map<String, ? extends Iterable>, but we can only supply Map<String, Object>
        assertThat(loggedMessage, not(hasKey("tags")));
    }

    @Test
    public void testJULSynchronous() {
        TestUtil.configLoggerFromPropertiesFile("/jul-synchronous.properties");

        Logger logger = Logger.getLogger("testJULSynchronous");

        MDC.put("keyOne", "valueOne");
        MDC.put("keyTwo", "valueTwo");

        logger.log(Level.FINE, "A simple test message", new RuntimeException("Test exception"));

        // pause to allow the message to be sent to redis
        TestUtil.pause(200);

        String lastEntry = TestUtil.readLastRedisEntry("logstash");
        assertThat("Could not read entry from redis", lastEntry, not(emptyOrNullString()));

        Map<String, Object> loggedMessage = TestUtil.jsonToMap(lastEntry);

        assertThat(loggedMessage, hasEntry("thread", "1")); // JUL only exposes the thread ID, not the thread name
        assertThat(loggedMessage, hasEntry("level", "FINE"));
        assertThat(loggedMessage, hasKey("message"));
        assertThat(loggedMessage, hasEntry("logger", "testJULSynchronous"));
        assertThat(loggedMessage, hasKey("exception"));

        // location is not in an object by default
        assertThat(loggedMessage, not(hasKey("location")));
        assertThat(loggedMessage, hasKey("class"));
        assertThat(loggedMessage, hasKey("method"));
//        assertThat(loggedMessage, hasKey("file")); // JUL doesn't expose access to the file
        assertThat(loggedMessage, hasKey("line"));

        assertThat(loggedMessage, hasKey("@timestamp"));

        // make sure mdc fields are logged as well. mdc is not in an object by default.
        assertThat(loggedMessage, not(hasKey("mdc")));
        assertThat(loggedMessage, hasEntry("keyOne", "valueOne"));
        assertThat(loggedMessage, hasEntry("keyTwo", "valueTwo"));

        // have to de-generify Matcher here because otherwise assertThat will expect Map<String, ? extends Iterable>, but we can only supply Map<String, Object>
        assertThat(loggedMessage, not(hasKey("tags")));
    }
}
