package com.gjb.pro.utils;

import com.alibaba.fastjson.JSONObject;


/**
 * Created by L.cm
 * Date: 2015-25-12 17:57
 */
public final class JsonUtils {
    private JsonUtils() {
    }

    /**
     * 将对象序列化成json字符串
     *
     * @param object javaBean
     * @return jsonString json字符串
     */
    public static String toJson(Object object) {

        return JSONObject.toJSONString(object);
    }

    /**
     * 将json反序列化成对象
     *
     * @param jsonString jsonString
     * @param valueType  class
     * @param <T>        T 泛型标记
     * @return Bean
     */
    public static <T> T parse(String jsonString, Class<T> valueType) {

        return JSONObject.parseObject(jsonString, valueType);
    }


}
