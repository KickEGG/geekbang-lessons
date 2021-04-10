package org.geektimes.cache.serialization;

import org.geektimes.cache.serialization.json.JsonSerialization;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author KickEGG
 * @createTime 2021-04-10 1:25 下午
 * @description
 * @keyPoint
 */
public class RedisSerializerFactory<T> {
    public static RedisSerializer getRedisSerializer() {
        ServiceLoader<RedisSerializer> serviceLoader = ServiceLoader.load(RedisSerializer.class);
        Iterator<RedisSerializer> redisSerializerIterator = serviceLoader.iterator();

        while (redisSerializerIterator.hasNext()) {
            RedisSerializer redisSerializer = redisSerializerIterator.next();
            return redisSerializer;
        }

        throw new RuntimeException("RedisSerializer 未找到实现");
    }
}
