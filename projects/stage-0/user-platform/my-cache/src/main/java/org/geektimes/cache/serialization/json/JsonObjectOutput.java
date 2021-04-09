package org.geektimes.cache.serialization.json;

import com.alibaba.fastjson.JSON;

import java.util.Collection;

/**
 * @author KickEGG
 * @createTime 2021-04-09 8:45 下午
 * @description
 * @keyPoint
 */
public class JsonObjectOutput<K> {
    Object object;

    public JsonObjectOutput(K object) {
        this.object = object;
    }

    public String objectOutput() {
        return JSON.toJSONString(object);
    }
}
