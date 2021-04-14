package org.geektimes.cache.serialization.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import org.geektimes.cache.serialization.RedisSerializer;

import javax.cache.CacheException;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;

/**
 * @author KickEGG
 * @createTime 2021-04-09 8:30 下午
 * @description 参考redisTemplate实现
 * @keyPoint org.geektimes.cache.serialization.RedisSerializer
 */
public class JsonSerialization<T> implements RedisSerializer<T> {

    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    private Class<T> clazz;

    public JsonSerialization(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    public JsonSerialization() {
        super();
    }

    @Override
    public byte[] serialize(T t) throws CacheException {
        if (null == t) {
            return new byte[0];
        }

        byte[] value = null;

        try {
            value = JSON.toJSONString(t, SerializerFeature.WriteClassName).getBytes(DEFAULT_CHARSET);
        } catch (Exception e) {
            throw new CacheException(e);
        }
        return value;
    }

    @Override
    public <T> T deserialize(byte[] bytes) throws CacheException {
        if (null == bytes || bytes.length <= 0) {
            return null;
        }

        T value = null;

        try {
            String str = new String(bytes, DEFAULT_CHARSET);
            value = (T) JSON.parseObject(str, clazz);
        } catch (Exception e) {
            throw new CacheException(e);
        }
        return value;
    }
}
