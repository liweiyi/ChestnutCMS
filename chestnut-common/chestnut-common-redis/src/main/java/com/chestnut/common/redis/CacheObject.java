package com.chestnut.common.redis;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

/**
 * 缓存数据
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class CacheObject<T> {

    private T data;

    private Long expiresIn;

    private TimeUnit timeUnit;

    public static <T> CacheObject<T> create(T data, Long expiresIn, TimeUnit timeUnit) {
        CacheObject<T> co = new CacheObject<>();
        co.data = data;
        co.expiresIn = expiresIn;
        co.timeUnit = timeUnit;
        return co;
    }
}
