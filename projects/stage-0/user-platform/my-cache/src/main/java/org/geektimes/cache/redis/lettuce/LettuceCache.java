package org.geektimes.cache.redis.lettuce;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import org.geektimes.cache.AbstractCache;
import org.geektimes.cache.ExpirableEntry;
import org.geektimes.cache.serialization.RedisSerializer;
import org.geektimes.cache.serialization.RedisSerializerFactory;

import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

/**
 * @author KickEGG
 * @createTime 2021-04-10 2:00 下午
 * @description 基于 Lettuce 的 redisCache 实现
 * @keyPoint Lettuce 的连接是基于 Netty 的
 */
public class LettuceCache<K extends Serializable, V extends Serializable> extends AbstractCache<K, V> {

    private RedisClient client;

    private final RedisSerializer redisSerializer = RedisSerializerFactory.getRedisSerializer();

    protected LettuceCache(CacheManager cacheManager,
                           String cacheName,
                           Configuration<K, V> configuration,
                           RedisClient client) {
        super(cacheManager, cacheName, configuration);
        this.client = client;
    }

    protected V doGet(byte[] jsonKey) {
        StatefulRedisConnection<K, V> connection = (StatefulRedisConnection<K, V>) client.connect();
        byte[] valueBytes = (byte[]) connection.sync().get((K) new String(jsonKey));
        V value = (V) redisSerializer.deserialize(valueBytes);
        connection.close();
        return value;
    }

    @Override
    protected boolean containsEntry(K key) throws CacheException, ClassCastException {
        return false;
    }

    @Override
    protected ExpirableEntry<K, V> getEntry(K key) throws CacheException, ClassCastException {
        return null;
    }

    @Override
    protected void putEntry(ExpirableEntry<K, V> entry) throws CacheException, ClassCastException {

    }

    @Override
    protected ExpirableEntry<K, V> removeEntry(K key) throws CacheException, ClassCastException {
        return null;
    }

    @Override
    protected void clearEntries() throws CacheException {

    }

    @Override
    protected Set<K> keySet() {
        return null;
    }
}
