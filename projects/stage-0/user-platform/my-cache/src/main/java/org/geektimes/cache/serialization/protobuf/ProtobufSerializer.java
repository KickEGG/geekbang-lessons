//package org.geektimes.cache.serialization.protobuf;

import org.geektimes.cache.serialization.RedisSerializer;

import javax.cache.CacheException;

///**
// * @author KickEGG
// * @createTime 2021-04-14 3:38 下午
// * @description
// * @keyPoint
// */
//public class ProtobufSerializer <T > implements RedisSerializer<T> {
//    public static volatile Map<String, Codec> simpleTypeCodeMap = new HashMap<>();
//    public static final Charset UTF8 = Charset.forName("UTF-8");
//    private Class<T> tClass;
//
//    public ProtobufRedisSerializer(Class<T> tClass) {
//        super();
//        this.tClass = tClass;
//    }
//
//    public ProtobufRedisSerializer(T t) {
//        super();
//        this.tClass = (Class<T>) t.getClass();
//    }
//
//    @Override
//    public byte[] serialize(T t) throws SerializationException {
//        Codec<T> codec = getCodec(t.getClass());
//        try {
//            return codec.encode(t);
//        } catch (IOException e) {
//            throw  new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public T deserialize(byte[] bytes) throws SerializationException {
//        if (ObjectUtils.isEmpty(bytes) || bytes.length == 0){
//            return null;
//        }
//        try {
//            Codec<T> codec = getCodec(tClass);
//            return codec.decode(bytes);
//        } catch (Exception e) {
//            throw  new CacheException(e);
//        }
//    }

//    private Codec<T> getCodec(Class clazz){
//        Codec codec = simpleTypeCodeMap.get(clazz.getTypeName());
//        if (ObjectUtils.isEmpty(codec)){
//            synchronized (ProtobufRedisSerializer.class) {
//                codec = Optional.ofNullable(codec).orElseGet(() -> ProtobufProxy.create(clazz));
//                simpleTypeCodeMap.put(tClass.getTypeName(),codec);
//            }
//        }
//        return codec;
//    }
//}
