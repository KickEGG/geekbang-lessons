package org.geektimes.cache.serialization;

import com.sun.xml.internal.ws.encoding.soap.SerializationException;

import javax.cache.CacheException;

/**
 * @author KickEGG
 * @createTime 2021-04-10 9:33 上午
 * @description
 * @keyPoint
 */
public interface RedisSerializer<T> {

    byte[] serialize(T t) throws CacheException;

    <T> T deserialize(byte[] bytes) throws CacheException;

}
