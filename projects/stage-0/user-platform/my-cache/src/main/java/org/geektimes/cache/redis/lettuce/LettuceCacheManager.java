package org.geektimes.cache.redis.lettuce;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import org.geektimes.cache.AbstractCacheManager;

import javax.cache.Cache;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;
import java.net.URI;
import java.util.Properties;

/**
 * @author KickEGG
 * @createTime 2021-04-10 2:00 下午
 * @description
 * @keyPoint 基于netty连接线程池..
 */
public class LettuceCacheManager extends AbstractCacheManager {

    private static final RedisClient client = RedisClient.create("redis://@127.0.0.1:6379/0");


    public LettuceCacheManager(CachingProvider cachingProvider, URI uri, ClassLoader classLoader, Properties properties) {
        super(cachingProvider, uri, classLoader, properties);
    }

    @Override
    protected <K, V, C extends Configuration<K, V>> Cache doCreateCache(String cacheName, C configuration) {
        StatefulRedisConnection<String, String> connection = client.connect();
        RedisStringCommands<String, String> sync = connection.sync();
        return new LettuceCache(this, cacheName, configuration,sync);
    }

    @Override
    protected void doClose() {
        client.shutdown();
    }
}
