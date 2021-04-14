package org.geektimes.cache.redis.lettuce;

import io.lettuce.core.codec.RedisCodec;
import org.geektimes.cache.serialization.RedisSerializer;
import org.geektimes.cache.serialization.RedisSerializerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author KickEGG
 * @createTime 2021-04-14 3:01 下午
 * @description
 * @keyPoint
 */
public class SerializedObjectCodec implements RedisCodec<String, Object> {
    private Charset charset = Charset.forName("UTF-8");

    @Override
    public String decodeKey(ByteBuffer bytes) {
        return charset.decode(bytes).toString();
    }

    @Override
    public Object decodeValue(ByteBuffer bytes) {
        try {
            byte[] array = new byte[bytes.remaining()];
            bytes.get(array);
            ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(array));
            return is.readObject();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ByteBuffer encodeKey(String key) {
        return charset.encode(key);
    }

    @Override
    public ByteBuffer encodeValue(Object value) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(bytes);
            os.writeObject(value);
            return ByteBuffer.wrap(bytes.toByteArray());
        } catch (IOException e) {
            return null;
        }
    }
}