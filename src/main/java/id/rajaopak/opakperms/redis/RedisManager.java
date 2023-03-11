package id.rajaopak.opakperms.redis;

import id.rajaopak.opakperms.OpakPerms;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedisManager {

    private final OpakPerms core;
    public ExecutorService REDIS_EXECUTOR = Executors.newSingleThreadExecutor();
    private boolean auth;
    private String password;
    private JedisPool subscriberPool;

    private JedisPool publisherPool;

    private JedisPubSub pubSub;
    private String channel;

    public RedisManager(OpakPerms core) {
        this.core = core;
    }

    public boolean connect(String host, int port, @Nullable String password, String channel) {
        this.channel = channel;
        this.password = password;
        this.auth = password != null && password.length() > 0;

        try {
            this.subscriberPool = this.publisherPool = new JedisPool(new JedisPoolConfig(), host, port, 30000, password);
            this.setupSub();
        } catch (Exception e) {
            this.subscriberPool = this.publisherPool = null;
            e.printStackTrace();
        }

        return isRedisConnected();
    }

    public boolean sendRequest(String message) {
        if (!this.isRedisConnected()) return false;

        try {
            if (message == null) {
                throw new IllegalStateException("Object that was being sent was null!");
            }

            try (Jedis jedis = this.publisherPool.getResource()) {
                try {
                    if (this.auth) {
                        jedis.auth(this.password);
                    }

                    jedis.publish(this.channel, message);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isRedisConnected() {
        return this.subscriberPool != null && !this.subscriberPool.isClosed() && this.publisherPool != null && !this.publisherPool.isClosed();
    }

    public void close() {
        try {
            if (this.pubSub != null && this.pubSub.isSubscribed()) {
                this.pubSub.unsubscribe();
                this.pubSub = null;
            }

            if (this.subscriberPool != null && !this.subscriberPool.isClosed()) {
                this.subscriberPool.destroy();
                this.subscriberPool = null;
            }

            if (this.publisherPool != null && !this.publisherPool.isClosed()) {
                this.publisherPool.destroy();
                this.publisherPool = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupSub() {
        this.pubSub = new PubSubListener(this.core);

        this.REDIS_EXECUTOR.execute(() -> {
            try {
                Jedis jedis = this.subscriberPool.getResource();
                Throwable throwable = null;

                try {
                    jedis.subscribe(this.pubSub, this.channel);
                } catch (Throwable e) {
                    throwable = e;
                    throw e;
                } finally {
                    if (jedis != null) {
                        if (throwable != null) {
                            try {
                                jedis.close();
                            } catch (Throwable e) {
                                throwable.addSuppressed(e);
                            }
                        } else {
                            jedis.close();
                        }
                    }
                }
            } catch (Exception e) {
                this.subscriberPool = this.publisherPool = null;
                e.printStackTrace();
            }
        });
    }

    public JedisPool getSubscriberPool() {
        return subscriberPool;
    }

    public JedisPool getPublisherPool() {
        return publisherPool;
    }

}
