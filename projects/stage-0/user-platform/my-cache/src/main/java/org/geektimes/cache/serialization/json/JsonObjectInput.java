package org.geektimes.cache.serialization.json;

import com.alibaba.fastjson.JSON;

/**
 * @author KickEGG
 * @createTime 2021-04-09 8:46 下午
 * @description
 * @keyPoint
 */
public class JsonObjectInput {
    Object object;

    public JsonObjectInput(Object object) {
        this.object = object;
    }

    public Object objectInput() {
        return JSON.parseObject(object.toString());
    }
}
