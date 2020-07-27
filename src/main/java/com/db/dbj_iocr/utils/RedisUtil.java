package com.db.dbj_iocr.utils;

/**
 * @ClassName RedisUtil
 * @Author 夏俭琼
 * @Date 2020/4/14 14:30
 **/

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @component （把普通pojo实例化到spring容器中，相当于配置文件中的
 * <bean id="" class=""/>）
 */
@Component
public class RedisUtil {

    @Autowired
    private static StringRedisTemplate stringRedisTemplate;

    public static boolean set(String key, Object value) {
        if (value == null) {
            value = "";
        }
        try {
            if (value instanceof String) {
                stringRedisTemplate.opsForValue().set(key, (String) value);
            } else {
                stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(value));
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean set(String key, Object value, long expire) {
        if (value == null) {
            value = "";
        }
        try {
            stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(value), expire, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public long getExpire(String key) {
        return stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
    }


    public static String get(String key) {
        if (StringUtils.isEmpty(key)) {
            return "";
        }
        try {
            return stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    public boolean remove(String key) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        try {
            return stringRedisTemplate.delete(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

}
