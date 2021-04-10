package org.geektimes.cache.serialization;

import com.sun.xml.internal.ws.encoding.soap.SerializationException;

/**
 * @author KickEGG
 * @createTime 2021-04-10 9:33 上午
 * @description
 * @keyPoint
 */
public interface RedisSerializer<T> {

    byte[] serialize(T t) throws SerializationException;

    T deserialize(byte[] bytes) throws SerializationException;

}
