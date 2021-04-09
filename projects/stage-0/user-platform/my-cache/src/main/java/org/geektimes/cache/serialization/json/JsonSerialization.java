package org.geektimes.cache.serialization.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.sun.xml.internal.ws.encoding.soap.SerializationException;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;

/**
 * @author KickEGG
 * @createTime 2021-04-09 8:30 下午
 * @description
 * @keyPoint
 */
public class JsonSerialization<T> implements Serializable {

    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    private Class<T> clazz;

    public JsonSerialization(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }


    public byte[] serialize(T t) throws SerializationException {
        if (null == t) {
            return new byte[0];
        }
        return JSON.toJSONString(t, SerializerFeature.WriteClassName).getBytes(DEFAULT_CHARSET);
    }

    public T deserialize(byte[] bytes) throws SerializationException {
        if (null == bytes || bytes.length <= 0) {
            return null;
        }
        String str = new String(bytes, DEFAULT_CHARSET);
        return (T) JSON.parseObject(str, clazz);
    }

}
