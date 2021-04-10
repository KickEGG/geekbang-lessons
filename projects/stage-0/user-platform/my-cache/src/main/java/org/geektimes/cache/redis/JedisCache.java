package org.geektimes.cache.redis;

import org.geektimes.cache.AbstractCache;
import org.geektimes.cache.serialization.RedisSerializer;
import org.geektimes.cache.serialization.RedisSerializerFactory;
import org.geektimes.cache.serialization.json.JsonSerialization;
import redis.clients.jedis.Jedis;

import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

public class JedisCache<K extends Serializable, V extends Serializable> extends AbstractCache<K, V> {

    private final Jedis jedis;

    private final RedisSerializer redisSerializer = RedisSerializerFactory.getRedisSerializer();

    public JedisCache(CacheManager cacheManager, String cacheName,
                      Configuration<K, V> configuration, Jedis jedis) {
        super(cacheManager, cacheName, configuration);
        this.jedis = jedis;
    }

//    @Override
//    protected V doGet(K key) throws CacheException, ClassCastException {
//        byte[] keyBytes = serialize(key);
//        return doGet(keyBytes);
//    }

//    @Override
//    protected V doGet(K key) throws CacheException, ClassCastException {
//        byte[] keyBytes = serialize(key);
//        return doGet(keyBytes);
//    }

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
        byte[] valueBytes = jedis.get(jsonKey);
        V value = (V) redisSerializer.deserialize(valueBytes);
        return value;
    }

//    protected V doGet(byte[] keyBytes) {
//        byte[] valueBytes = jedis.get(keyBytes);
//        V value = deserialize(valueBytes);
//        return value;
//    }

//    @Override
//    protected V doPut(K key, V value) throws CacheException, ClassCastException {
//
//        JsonSerialization jsonSerialization = new JsonSerialization();
//        byte[] keyBytes = jsonSerialization.serialize(key);
//        byte[] valueBytes = serialize(value);
//        V oldValue = doGet(keyBytes);
//        jedis.set(keyBytes, valueBytes);
//        return oldValue;
//    }

    @Override
    protected V doPut(K key, V value) throws CacheException, ClassCastException, IOException {
        V oldValue = doGet(redisSerializer.serialize(key));
        jedis.set(redisSerializer.serialize(key),
                redisSerializer.serialize(value));
        return oldValue;
    }


//    protected V doGet(String jsonKey) {
//        String jsonValue = jedis.get(jsonKey);
//        JsonSerialization jsonSerialization = new JsonSerialization();
//        V value = null;
//
//        try {
//            value = (V) jsonSerialization.deserialize(jsonValue);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return value;
//    }

    private Class getClassType(Object object) {
        Type type = object.getClass().getGenericSuperclass(); // generic 泛型
        if (type instanceof ParameterizedType) {
            // 强制转化“参数化类型”
            ParameterizedType parameterizedType = (ParameterizedType) type;
            // 参数化类型中可能有多个泛型参数
            Type[] types = parameterizedType.getActualTypeArguments();
            // 获取数据的第一个元素(User.class)
            return (Class<K>) types[0];
        }
        return null;
    }

    @Override
    protected V doRemove(K key) throws CacheException, ClassCastException {
        byte[] keyBytes = redisSerializer.serialize(key);
        V oldValue = doGet(keyBytes);
        jedis.del(keyBytes);
        return oldValue;
    }

    @Override
    protected void doClear() throws CacheException {

    }

    @Override
    protected Iterator<Entry<K, V>> newIterator() {
        return null;
    }

    @Override
    protected void doClose() {
        this.jedis.close();
    }

    // TODO 是否可以抽象出一套序列化和反序列化的 API
    private byte[] serialize(Object value) throws CacheException {
        byte[] bytes = null;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)
        ) {
            // Key -> byte[]
            objectOutputStream.writeObject(value);
            bytes = outputStream.toByteArray();
        } catch (IOException e) {
            throw new CacheException(e);
        }
        return bytes;
    }

    private V deserialize(byte[] bytes) throws CacheException {
        if (bytes == null) {
            return null;
        }
        V value = null;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)
        ) {
            // byte[] -> Value
            value = (V) objectInputStream.readObject();
        } catch (Exception e) {
            throw new CacheException(e);
        }
        return value;
    }

}
