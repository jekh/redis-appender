package org.jekh.appenders.jul;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import redis.clients.jedis.Jedis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TempPerfTest {
    private static final String key = "logstash";
    private static final int parallel = 50;
    private static final int statements = 100000;

    private static final AtomicInteger statementsLeft = new AtomicInteger(statements);

    public static void main(String[] args) {
        try {
            executeTestUsingJUL();
            executeTestUsingSlf4j();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void executeTestUsingSlf4j() throws InterruptedException {
        statementsLeft.set(statements);

        System.out.println("Before Test, clearing Redis");
        try (Jedis redis = new Jedis("localhost")) {
            // clear the redis list first
            redis.ltrim(key, 1, 0);

            long size0 = redis.llen(key);
            System.out.println("Redis Log Size: " + size0);

            TestUtil.configLoggerFromPropertiesFile("/jul-defaults.properties");

            Logger logger = LoggerFactory.getLogger(TempPerfTest.class);


            ExecutorService executorService = Executors.newFixedThreadPool(parallel);

            Runnable task = () -> {
                for (int iteration = 0; statementsLeft.getAndDecrement() > 0; iteration++) {
                    MDC.put("mdcvar1", "test1");
                    MDC.put("mdcvar2", "test2");
                    logger.debug("Test MDC Log {}", iteration);
                }
            };

            long start = System.currentTimeMillis();

            for (int i = 0; i < parallel; i++) {
                executorService.execute(task);
            }

            executorService.shutdown();
            executorService.awaitTermination(30, TimeUnit.SECONDS);

            long finish = System.currentTimeMillis();

            // probably not immediately have the same size
            long size = redis.llen(key);
            System.out.println("Log Size: " + size);
//        assertTrue(size0 < 100);

            Thread.sleep(2000);

            long size1 = redis.llen(key);
            System.out.println("Log Size After Wait: " + size1);
//        assertTrue(size0 < size1);

            System.out.println("Time to complete: " + (finish - start) + "ms");
        }

    }

    public static void executeTestUsingJUL() throws InterruptedException {
        statementsLeft.set(statements);

        System.out.println("Before Test, clearing Redis");
        try (Jedis redis = new Jedis("localhost")) {
            // clear the redis list first
            redis.ltrim(key, 1, 0);

            long size0 = redis.llen(key);
            System.out.println("Redis Log Size: " + size0);

            TestUtil.configLoggerFromPropertiesFile("/jul-defaults.properties");

            java.util.logging.Logger logger = java.util.logging.Logger.getLogger("TempPerfTest");


            ExecutorService executorService = Executors.newFixedThreadPool(parallel);

            Runnable task = () -> {
                for (int iteration = 0; statementsLeft.getAndDecrement() > 0; iteration++) {
                    MDC.put("mdcvar1", "test1");
                    MDC.put("mdcvar2", "test2");
                    logger.fine("Test MDC Log " + iteration);
                }
            };

            long start = System.currentTimeMillis();

            for (int i = 0; i < parallel; i++) {
                executorService.execute(task);
            }

            executorService.shutdown();
            executorService.awaitTermination(30, TimeUnit.SECONDS);

            long finish = System.currentTimeMillis();

            // probably not immediately have the same size
            long size = redis.llen(key);
            System.out.println("Log Size: " + size);
//        assertTrue(size0 < 100);

            Thread.sleep(2000);

            long size1 = redis.llen(key);
            System.out.println("Log Size After Wait: " + size1);
//        assertTrue(size0 < size1);

            System.out.println("Time to complete: " + (finish - start) + "ms");
        }

    }
}
