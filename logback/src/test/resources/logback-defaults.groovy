import org.jekh.appenders.logback.LogbackRedisAppender
import org.jekh.appenders.logback.LogbackLogstashLayout

import static ch.qos.logback.classic.Level.*

appender("redis-appenders", LogbackRedisAppender)

root(TRACE, ["redis-appenders"])