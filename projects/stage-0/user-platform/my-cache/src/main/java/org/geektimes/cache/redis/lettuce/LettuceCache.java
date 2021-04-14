package org.geektimes.cache.redis.lettuce;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.geektimes.cache.AbstractCache;
import org.geektimes.cache.ExpirableEntry;
import org.geektimes.cache.serialization.RedisSerializer;
import org.geektimes.cache.serialization.RedisSerializerFactory;

import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import java.io.Serializable;
import java.util.Set;

/**
 * @author KickEGG
 * @createTime 2021-04-10 2:00 下午
 * @description 基于 Lettuce 的 redisCache 实现
 * @keyPoint Lettuce 的连接是基于 Netty 的
 */
public class LettuceCache<K extends Serializable, V extends Serializable> extends AbstractCache<K, V> {

    private RedisClient client;

    private final LettuceCodec<K, V> lettuceCodec = new LettuceCodec<K, V>();

    protected LettuceCache(CacheManager cacheManager,
                           String cacheName,
                           Configuration<K, V> configuration,
                           RedisClient client) {
        super(cacheManager, cacheName, configuration);
        this.client = client;
    }

    protected V doGet(byte[] jsonKey) {
        StatefulRedisConnection connection = client.connect(lettuceCodec);
        V value = (V) connection.sync().get(jsonKey);
        connection.close();
        return value;
    }

    @Override
    protected boolean containsEntry(K key) throws CacheException, ClassCastException {
        StatefulRedisConnection connection = client.connect(lettuceCodec);
        if (connection.sync().get(key) != null) {
            connection.close();
            return true;
        } else {
            connection.close();
            return false;
        }
    }

    @Override
    protected ExpirableEntry<K, V> getEntry(K key) throws CacheException, ClassCastException {
        StatefulRedisConnection connection = client.connect(lettuceCodec);
        V value = (V) connection.sync().get(key);
        connection.close();
        return ExpirableEntry.of((K) key,
                (V) value);
    }

    @Override
    protected void putEntry(ExpirableEntry<K, V> entry) throws CacheException, ClassCastException {
        StatefulRedisConnection connection = client.connect(lettuceCodec);
        connection.sync().set(entry.getKey(), entry.getValue());
        connection.close();
    }

    @Override
    protected ExpirableEntry<K, V> removeEntry(K key) throws CacheException, ClassCastException {
        StatefulRedisConnection connection = client.connect(lettuceCodec);
        ExpirableEntry<K, V> oldEntry = getEntry(key);
        connection.sync().del(key);
        connection.close();
        return oldEntry;
    }

    @Override
    protected void clearEntries() throws CacheException {

    }

    @Override
    protected Set<K> keySet() {
        return null;
    }
}
