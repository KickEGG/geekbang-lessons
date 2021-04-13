package org.geektimes.cache.redis.lettuce;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.api.sync.RedisStringCommands;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.geektimes.cache.AbstractCacheManager;
import redis.clients.jedis.JedisPool;

import javax.cache.Cache;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;
import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Properties;

/**
 * @author KickEGG
 * @createTime 2021-04-10 2:00 下午
 * @description
 * @keyPoint 可以优化为基于netty连接线程池..
 */
public class LettuceCacheManager extends AbstractCacheManager {

    private static final RedisClient client = RedisClient.create(
            RedisURI.builder()                    // <1> 创建单机连接的连接信息
                    .withHost("localhost")
                    .withPort(6379)
                    .withTimeout(Duration.of(10, ChronoUnit.SECONDS))
                    .build());

    public LettuceCacheManager(CachingProvider cachingProvider, URI uri, ClassLoader classLoader, Properties properties) {
        super(cachingProvider, uri, classLoader, properties);
    }

    static final RedisCodec<byte[], byte[]> CODEC = ByteArrayCodec.INSTANCE;

    @Override
    protected <K, V, C extends Configuration<K, V>> Cache doCreateCache(String cacheName, C configuration) {
//        StatefulRedisConnection<K, V> connection = (StatefulRedisConnection<K, V>) client.connect();
        return new LettuceCache(this, cacheName, configuration, client);
    }


    @Override
    protected void doClose() {
        client.shutdown();
    }
}
