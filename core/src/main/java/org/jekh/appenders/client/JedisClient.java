package org.jekh.appenders.client;

import org.jekh.appenders.Defaults;
import org.jekh.appenders.exception.ExceptionUtil;
import org.jekh.appenders.log.SimpleLog;
import org.vibur.objectpool.ConcurrentPool;
import org.vibur.objectpool.PoolObjectFactory;
import org.vibur.objectpool.PoolService;
import org.vibur.objectpool.util.ConcurrentLinkedQueueCollection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class JedisClient implements RedisClient {
    /**
     * Unique identifier for each JedisClient instance, to allow differentiation when multiple appenders are used.
     */
    private static final AtomicInteger jedisClientCounter = new AtomicInteger(0);

    private final int jedisClientId = jedisClientCounter.getAndIncrement();

    private PoolService<Jedis> jedisPool;

    private final byte[] redisKeyAsBytes;

    // redis client connection settings
    private final String host;
    private final int port;
    private final int timeoutMs;
    private final String password;
    private final int database;
    private final String clientName;
    private final boolean tls;
    private final Charset charset;

    private final BlockingQueue<byte[]> queue;

    private final boolean synchronous;
    private final int threads;
    private final int logBatchSize;
    private final List<Thread> asyncWorkerThreads;

    private final int maxThreadBlockTimeMs;
    private final int workerTimeoutMs;

    /**
     * When true, prints Jedis client information to stdout for troubleshooting/debugging purposes.
     */
    private final boolean debug;

    private volatile boolean stopped = false;

    /**
     * Creates an asynchronous JedisClient. In general, use {@link RedisClientBuilder} to create new instances of {@link RedisClient}.
     * See the javadoc at {@link RedisClientBuilder} for a description of these parameters.
     */
    public JedisClient(String redisKey, String host, int port, boolean tls, int timeout, String password, int database,
                       String clientName, Charset charset, int threads, int logBatchSize, int maxLogQueueSize,
                       int maxThreadBlockTimeMs, int workerTimeoutMs, boolean debug) {
        if (debug) {
            SimpleLog.debug("Creating asynchronous JedisClient with " + threads + " threads connecting to redis at " + host + ":" + port
                    + " (database=" + database + ", redisKey=" + redisKey + ", clientName=" + clientName + ", charset=" + charset
                    + ", password=" + (password == null ? "null" : "[hidden]") + ", socket and connection timeout=" + timeout + "ms"
                    + ", max log messages per push to redis=" + logBatchSize + ", max log messages to queue=" + maxLogQueueSize
                    + ", max app thread block time=" + (maxThreadBlockTimeMs == Integer.MAX_VALUE ? "indefinite" : maxThreadBlockTimeMs + "ms")
                    + ", redis push timeout=" + (workerTimeoutMs == Integer.MAX_VALUE ? "indefinite" : workerTimeoutMs + "ms")
                    + ")");
        }

        this.redisKeyAsBytes = redisKey.getBytes(charset);
        this.host = host;
        this.port = port;
        this.timeoutMs = timeout;
        this.password = password;
        this.database = database;
        this.clientName = clientName;
        this.tls = tls;
        this.charset = charset;

        this.debug = debug;

        synchronous = false;

        this.threads = threads;
        this.logBatchSize = logBatchSize;
        this.asyncWorkerThreads = new ArrayList<>(threads);

        //TODO: measure performance of LinkedBlockingQueue vs. ArrayBlockingQueue. anecdotal evidence suggests ArrayBlockingQueue is slightly faster.
        this.queue = new ArrayBlockingQueue<>(maxLogQueueSize);

        this.maxThreadBlockTimeMs = maxThreadBlockTimeMs;
        this.workerTimeoutMs = workerTimeoutMs;
    }

    /**
     * Creates a synchronous JedisClient. In general, use {@link RedisClientBuilder} to create new instances of {@link RedisClient}.
     * See the javadoc at {@link RedisClientBuilder} for a description of these parameters.
     */
    public JedisClient(String redisKey, String host, int port, boolean tls, int timeout, String password, int database,
                       String clientName, Charset charset, int maxThreadBlockTimeMs, boolean debug) {
        if (debug) {
            SimpleLog.debug("Creating synchronous JedisClient connecting to redis at " + host + ":" + port
                    + " (database=" + database + ", redisKey=" + redisKey + ", clientName=" + clientName + ", charset=" + charset
                    + ", password=" + (password == null ? "null" : "[hidden]") + ", socket and connection timeout=" + timeout + "ms"
                    + ", max app thread block time=" + (maxThreadBlockTimeMs == Integer.MAX_VALUE ? "indefinite" : maxThreadBlockTimeMs + "ms")
                    + ")");
        }

        this.redisKeyAsBytes = redisKey.getBytes(charset);
        this.host = host;
        this.port = port;
        this.timeoutMs = timeout;
        this.password = password;
        this.database = database;
        this.clientName = clientName;
        this.tls = tls;
        this.charset = charset;

        this.debug = debug;

        this.queue = null;
        this.synchronous = true;
        this.asyncWorkerThreads = null;

        this.threads = 0;
        this.logBatchSize = 0;

        this.maxThreadBlockTimeMs = maxThreadBlockTimeMs;
        this.workerTimeoutMs = 0;
    }

    @Override
    public long sendToRedis(String string) {
        return sendToRedis(string.getBytes(charset));
    }

    @Override
    public long sendToRedis(byte[] bytes) {
        if (synchronous) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.tryTake(maxThreadBlockTimeMs, TimeUnit.MILLISECONDS);

                if (jedis == null) {
                    // take() can return null when it is interrupted or times out while waiting for a pooled object
                    SimpleLog.warn("Interrupted while waiting for a redis client instance. Discarding log message: " + new String(bytes, charset));

                    return 0;
                } else {
                    long listLength = jedis.rpush(redisKeyAsBytes, bytes);

                    return listLength;
                }
            } catch (JedisException e) {
                // there isn't much we can do. potentially we could retry the request, but that functionality is better-suited for the async logger.
                if (debug) {
                    SimpleLog.warn(getRedisPushErrorMessage(Collections.singletonList(bytes), e));
                } else {
                    SimpleLog.warn("Unable to send log to redis. Discarding 1 message. Exception: " + e.getMessage());
                }

                return 0;
            } finally {
                if (jedis != null) {
                    jedisPool.restore(jedis);
                }
            }
        } else {
            // put() the message onto the queue. if the queue is full, this method will block until the worker threads process some of the log messages.
            try {
                boolean added = queue.offer(bytes, maxThreadBlockTimeMs, TimeUnit.MILLISECONDS);
                if (!added) {
                    SimpleLog.warn("Could not queue message to send to redis. Log queue is full. Discarding log message: " + new String(bytes, charset));
                }
            } catch (InterruptedException e) {
                SimpleLog.warn("Interrupted while attempting to send a log message to redis. Discarding log message: " + new String(bytes, charset));
            }

            return 0;
        }
    }

    @Override
    public void start() {
        if (synchronous) {
            jedisPool = new ConcurrentPool<>(new ConcurrentLinkedQueueCollection<>(), new JedisObjectFactory(),
                    Defaults.INITIAL_SYNC_CLIENT_POOL_SIZE,
                    Defaults.MAX_SYNC_CLIENT_POOL_SIZE,
                    false);
        } else {
            // create the jedis pool with an initial size of 0, to prevent the pool from failing if redis is currently down
            new ConcurrentPool<>(new ConcurrentLinkedQueueCollection<>(), new JedisObjectFactory(),
                    0,
                    threads,
                    false);

            Runnable pushToRedisWorker = new PushToRedisWorker();

            for (int threadNumber = 0; threadNumber < threads; threadNumber++) {
                Thread thread = new Thread(pushToRedisWorker);
                thread.setDaemon(true);
                thread.setName("redis-logger-client-" + jedisClientId + "-thread-" + threadNumber);
                thread.start();
                asyncWorkerThreads.add(thread);
            }
        }
    }

    @Override
    public void stop() {
        stopped = true;

        if (asyncWorkerThreads != null) {
            for (Thread thread : asyncWorkerThreads) {
                if (thread.isAlive()) {
                    thread.interrupt();
                }
            }
        }

        jedisPool.terminate();
    }

    /**
     * @return an error message including the specified log statements and the stack trace of t
     */
    private String getRedisPushErrorMessage(List<byte[]> logStatements, Throwable t) {
        StringBuilder errorMessage = new StringBuilder("Exception occurred while attempting to send log statements to redis: ");

        errorMessage.append(ExceptionUtil.getStackTraceAsString(t));

        if (logStatements.size() > 0) {
            errorMessage
                    .append(logStatements.size())
                    .append(" log statements may be lost. All log statements:\n");
            errorMessage.append(logStatements.stream()
                    .map(stmt -> new String(stmt, charset))
                    .collect(Collectors.joining("\n")));
        }

        return errorMessage.toString();
    }

    /**
     * Encapsulates the code to push log messages to redis asynchronously. The run() method will continue to push log messages
     * to redis until the {@link #stopped} field is set to true.
     */
    private class PushToRedisWorker implements Runnable {
        @Override
        public void run() {
            // synchronous == true and queue == null should always be both true or both false
            if (synchronous || queue == null) {
                throw new RuntimeException("Attempted to execute PushToRedisWorker thread when asynchronous redis push is disabled");
            }

            while (!stopped) {
                List<byte[]> toRedis = new ArrayList<>(logBatchSize);

                try {
                    toRedis.add(queue.take());
                    queue.drainTo(toRedis, logBatchSize - 1);

                    Jedis jedis = null;
                    try {
                        jedis = jedisPool.tryTake(workerTimeoutMs, TimeUnit.MILLISECONDS);

                        // take() can return null when it is interrupted or times out while waiting for a pooled object
                        if (jedis == null) {
                            if (debug) {
                                SimpleLog.debug("Interrupted while waiting for a redis client instance. Placing " + toRedis.size() + " messages on queue to retry.");
                            }

                            try {
                                queue.addAll(toRedis);
                            } catch (IllegalStateException e) {
                                if (debug) {
                                    // even though we're logging a "warning" due to a failure to connect to redis, this will result in an enormous amount of log spam
                                    // in a high-volume application.
                                    SimpleLog.warn(getRedisPushErrorMessage(toRedis, e));
                                } else {
                                    SimpleLog.warn("Unable re-add messages to queue after failure to obtain redis client. Discarding " + toRedis.size() + " log messages. Exception: " + e.getMessage());
                                }
                            }
                        } else {
                            jedis.rpush(redisKeyAsBytes, toRedis.toArray(new byte[toRedis.size()][]));
                        }
                    } catch (JedisException e) {
                        if (debug) {
                            SimpleLog.debug("Could not send " + toRedis.size() + " log messages to redis. Placing message on queue to retry. " + ExceptionUtil.getStackTraceAsString(e));
                        }

                        try {
                            queue.addAll(toRedis);
                        } catch (IllegalStateException queueForRetryException) {
                            // the queue is full. there's not much else we can do, other than log a warning.
                            e.addSuppressed(queueForRetryException);
                            if (debug) {
                                // even though we're logging a "warning" due to a failure to connect to redis, this will result in an enormous amount of log spam
                                // in a high-volume application.
                                SimpleLog.warn(getRedisPushErrorMessage(toRedis, e));
                            } else {
                                SimpleLog.warn("Unable to send messages to redis. Discarding " + toRedis.size() + " log messages. Exception: " + e.getMessage());
                            }
                        }
                    } finally {
                        if (jedis != null) {
                            jedisPool.restore(jedis);
                        }
                    }
                } catch (RuntimeException e) {
                    // some exception occurred while attempting to retrieve messages from the queue, obtain the redis client, and/or send the log
                    // statements to redis. there isn't much we can do other than print the error message and continue processing.
                    SimpleLog.warn(getRedisPushErrorMessage(toRedis, e));
                } catch (InterruptedException e) {
                    // if this thread was interrupted after stopping the client, we can abort. otherwise, continue until this client is stopped.
                    if (stopped) {
                        break;
                    }
                }
            }

            if (debug) {
                SimpleLog.debug("Ending redis push thread after interruption because redis client is stopped.");
            }
        }
    }

    /**
     * Factory for Jedis objects. Heavily modelled on {@link redis.clients.jedis.JedisFactory}, which does the same thing
     * using Commons Pool 2.
     */
    private class JedisObjectFactory implements PoolObjectFactory<Jedis> {
        @Override
        public Jedis create() {
            Jedis jedis = new Jedis(host, port, timeoutMs, timeoutMs, tls);

            try {
                jedis.connect();
                if (password != null) {
                    jedis.auth(password);
                }
                if (database != 0) {
                    jedis.select(database);
                }
                if (clientName != null) {
                    jedis.clientSetname(clientName);
                }
            } catch (JedisException je) {
                try {
                    jedis.close();
                } catch (RuntimeException e) {
                    je.addSuppressed(e);
                }

                throw je;
            }

            return jedis;
        }

        @Override
        public boolean readyToTake(Jedis jedis) {
            //TODO: examine performance implications of this - it calls ping() every time an object is taken from the pool
            return isValid(jedis);
        }

        @Override
        public boolean readyToRestore(Jedis jedis) {
            // if we are validating on readyToTake(), there's no need to validate when returning to the pool
            return true;
        }

        @Override
        public void destroy(Jedis jedis) {
            if (jedis.isConnected()) {
                try {
                    jedis.quit();
                    jedis.disconnect();
                } catch (RuntimeException e) {
                    // since the pool (or at least this client) is being destroyed, don't throw the exception. print an error message and continue.
                    if (debug) {
                        SimpleLog.warn("An error occurred closing the connection to redis. " + ExceptionUtil.getStackTraceAsString(e));
                    }
                }
            }
        }

        private boolean isValid(Jedis jedis) {
            try {
                return jedis.isConnected() && jedis.ping().equals("PONG");
            } catch (RuntimeException e) {
                return false;
            }
        }
    }
}
