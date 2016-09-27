import org.jekh.appenders.logback.LogbackLogstashLayout
import org.jekh.appenders.logback.LogbackRedisAppender

import static ch.qos.logback.classic.Level.TRACE

appender("redis-appenders", LogbackRedisAppender) {
    redisKey = "logstash"
    host = "localhost"
    port = 6379
    timeoutMs = 2000
    password = null
    database = 0
    clientName = null
    synchronous = false
    tls = false
    redisPushThreads = 5
    maxMessagesPerPush = 100
    logQueueSize = 100000
    charset = "UTF-8"
    debugAppender = true
    maxThreadBlockTimeMs = -1
    workerTimeoutMs = -1

    layout(LogbackLogstashLayout) {
        tags = ["one", "2", "three"]
        mdcInclude = ["includedkey"]
        mdcExclude = ["excludedkey"]
        additionalFields = [
                "source"        : "somesource",
                "path"          : "somepath",
                "type"          : "sometype",
                "host"          : "somehost",
                "otherkey"      : "othervalue",
                "frommdc"       : "MDC included value is @{includedkey} and MDC excluded value is @{excludedkey}",
                "defaultValue"  : "Default @{nonexistent:-value}",
                "noDefaultValue": "No default @{value}"
        ]
        mdcAsObject = true
        locationAsObject = true
        locationDepth = 0
        suppressFields = []

        fieldNames = [
                thread    : "theThread",
                level     : "severity",
                message   : "logMessage",
                logger    : "loggerName",
                exception : "throwable",
                location  : "logLocation",
                timestamp : "time",
                classField: "className",
                method    : "function",
                file      : "javafile",
                line      : "linenumber",
                tags      : "flags",
                mdc       : "mdcProperties"
        ]
    }
}

root(TRACE, ["redis-appenders"])
