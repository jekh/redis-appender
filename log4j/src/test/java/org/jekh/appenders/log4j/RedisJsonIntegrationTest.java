package org.jekh.appenders.log4j;

import org.hamcrest.Matcher;
import org.jekh.appenders.log4j.test.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;

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
    public void testSetAllSettingsInLog4jXml() {
        // basic test that all settings from the config are read and applied correctly
        TestUtil.configLoggerFromXml("/log4j-allsettings.xml");

        Logger logger = LoggerFactory.getLogger("testSetAllSettingsInLog4jXml");

        MDC.put("includedkey", "includedvalue");
        MDC.put("excludedkey", "excludedvalue");

        logger.debug("A simple test message", new RuntimeException("Test exception"));

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

        assertThat(loggedMessage, hasEntry("theThread", "main"));
        assertThat(loggedMessage, hasEntry("severity", "DEBUG"));
        assertThat(loggedMessage, hasKey("logMessage"));
        assertThat(loggedMessage, hasEntry("loggerName", "testSetAllSettingsInLog4jXml"));
        assertThat(loggedMessage, hasKey("throwable"));
        assertThat(loggedMessage, hasKey("logLocation"));

        Map<?, ?> location = (Map<?, ?>) loggedMessage.get("logLocation");
        assertThat(location, hasKey("className"));
        assertThat(location, hasKey("function"));
        assertThat(location, hasKey("javafile"));
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
    public void testDefaultLog4jXml() {
        TestUtil.configLoggerFromXml("/log4j-defaults.xml");

        Logger logger = LoggerFactory.getLogger("testDefaultLog4jXml");

        MDC.put("keyOne", "valueOne");
        MDC.put("keyTwo", "valueTwo");

        logger.debug("A simple test message", new RuntimeException("Test exception"));

        // pause to allow the message to be sent to redis
        TestUtil.pause(200);

        String lastEntry = TestUtil.readLastRedisEntry("logstash");
        assertThat("Could not read entry from redis", lastEntry, not(emptyOrNullString()));

        Map<String, Object> loggedMessage = TestUtil.jsonToMap(lastEntry);

        assertThat(loggedMessage, hasEntry("thread", "main"));
        assertThat(loggedMessage, hasEntry("level", "DEBUG"));
        assertThat(loggedMessage, hasKey("message"));
        assertThat(loggedMessage, hasEntry("logger", "testDefaultLog4jXml"));
        assertThat(loggedMessage, hasKey("exception"));

        // location is not in an object by default
        assertThat(loggedMessage, not(hasKey("location")));
        assertThat(loggedMessage, hasKey("class"));
        assertThat(loggedMessage, hasKey("method"));
        assertThat(loggedMessage, hasKey("file"));
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
    public void testSuppressFieldsInLog4jXml() {
        TestUtil.configLoggerFromXml("/log4j-suppressfields.xml");

        Logger logger = LoggerFactory.getLogger("testSuppressFieldsInLog4jXml");

        MDC.put("keyOne", "valueOne");
        MDC.put("keyTwo", "valueTwo");

        logger.debug("A simple test message", new RuntimeException("Test exception"));

        // pause to allow the message to be sent to redis
        TestUtil.pause(200);

        String lastEntry = TestUtil.readLastRedisEntry("logstash");
        assertThat("Could not read entry from redis", lastEntry, not(emptyOrNullString()));

        Map<String, Object> loggedMessage = TestUtil.jsonToMap(lastEntry);

        assertThat(loggedMessage, hasEntry("thread", "main"));
        assertThat(loggedMessage, hasEntry("level", "DEBUG"));
        assertThat(loggedMessage, hasKey("message"));
        assertThat(loggedMessage, hasEntry("logger", "testSuppressFieldsInLog4jXml"));

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
    public void testLog4jXmlWithProperties() {
        System.setProperty("SYSTEM_PROPERTY_1", "value from system property");

        TestUtil.configLoggerFromXml("/log4j-properties.xml");

        Logger logger = LoggerFactory.getLogger("testLog4jXmlWithProperties");

        logger.debug("A simple test message", new RuntimeException("Test exception"));

        // pause to allow the message to be sent to redis
        TestUtil.pause(200);

        String lastEntry = TestUtil.readLastRedisEntry("logstash");
        assertThat("Could not read entry from redis", lastEntry, not(emptyOrNullString()));

        Map<String, Object> loggedMessage = TestUtil.jsonToMap(lastEntry);

        assertThat(loggedMessage, hasEntry("thread", "main"));
        assertThat(loggedMessage, hasEntry("level", "DEBUG"));
        assertThat(loggedMessage, hasKey("message"));
        assertThat(loggedMessage, hasEntry("logger", "testLog4jXmlWithProperties"));
        assertThat(loggedMessage, hasKey("exception"));

        // location is not in an object by default
        assertThat(loggedMessage, not(hasKey("location")));
        assertThat(loggedMessage, hasKey("class"));
        assertThat(loggedMessage, hasKey("method"));
        assertThat(loggedMessage, hasKey("file"));
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

}