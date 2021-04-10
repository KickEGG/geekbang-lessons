package org.geektimes.cache.redis.lettuce;

import io.lettuce.core.api.sync.RedisStringCommands;
import org.geektimes.cache.AbstractCache;
import org.geektimes.cache.serialization.RedisSerializer;
import org.geektimes.cache.serialization.RedisSerializerFactory;

import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

/**
 * @author KickEGG
 * @createTime 2021-04-10 2:00 下午
 * @description 基于 Lettuce 的 redisCache 实现
 * @keyPoint Lettuce 的连接是基于 Netty 的
 */
public class LettuceCache<K extends Serializable, V extends Serializable> extends AbstractCache<K, V> {

    private RedisStringCommands<K, V> redisStringCommands;

    private final RedisSerializer redisSerializer = RedisSerializerFactory.getRedisSerializer();

    protected LettuceCache(CacheManager cacheManager,
                           String cacheName,
                           Configuration<K, V> configuration,
                           RedisStringCommands<K, V> redisStringCommands) {
        super(cacheManager, cacheName, configuration);
        this.redisStringCommands = redisStringCommands;
    }

    @Override
    protected V doGet(K key) throws CacheException, ClassCastException {
        byte[] jsonKey = null;
        try {
            jsonKey = redisSerializer.serialize(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doGet(jsonKey);
    }

    protected V doGet(byte[] jsonKey) {
        byte[] valueBytes = (byte[]) redisStringCommands.get((K) jsonKey);
        V value = (V) redisSerializer.deserialize(valueBytes);
        return value;
    }

    @Override
    protected V doPut(K key, V value) throws CacheException, ClassCastException, IOException {
        V oldValue = doGet(redisSerializer.serialize(key));
        redisStringCommands.set(
                (K) redisSerializer.serialize(key),
                (V) redisSerializer.serialize(value));
        return oldValue;
    }

    @Override
    protected V doRemove(K key) throws CacheException, ClassCastException {
        return null;
    }

    @Override
    protected void doClear() throws CacheException {

    }

    @Override
    protected Iterator<Entry<K, V>> newIterator() {
        return null;
    }
}
