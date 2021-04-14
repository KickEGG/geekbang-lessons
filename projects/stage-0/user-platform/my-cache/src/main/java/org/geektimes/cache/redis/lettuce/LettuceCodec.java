package org.geektimes.cache.redis.lettuce;

import com.alibaba.fastjson.JSON;
import io.lettuce.core.codec.RedisCodec;
import org.geektimes.cache.serialization.RedisSerializer;
import org.geektimes.cache.serialization.RedisSerializerFactory;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * @author KickEGG
 * @createTime 2021-04-14 10:43 上午
 * @description
 * @keyPoint
 */
public class LettuceCodec<K, V> implements RedisCodec<K, V> {

    private final RedisSerializer redisSerializer = RedisSerializerFactory.getRedisSerializer();

    @Override
    public K decodeKey(ByteBuffer buffer) {
        try {
            byte[] array = new byte[buffer.remaining()];
            buffer.get(array);
            return (K) redisSerializer.deserialize(array, array.getClass());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public V decodeValue(ByteBuffer buffer) {
        try {
            byte[] array = new byte[buffer.remaining()];
            buffer.get(array);
            return (V) redisSerializer.deserialize(array, array.getClass());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ByteBuffer encodeKey(K key) {
        try {
            return ByteBuffer.wrap(redisSerializer.serialize(key));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ByteBuffer encodeValue(Object value) {
        try {
            return ByteBuffer.wrap(redisSerializer.serialize(value));
        } catch (Exception e) {
            return null;
        }
    }
}
