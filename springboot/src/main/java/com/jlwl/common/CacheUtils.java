package com.jlwl.common;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 缓存工具 - Redis 不可用时自动降级（不抛异常）。
 */
@Component
public class CacheUtils {

    private final StringRedisTemplate redis;

    public CacheUtils(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void set(String key, String value, long timeoutSec) {
        try {
            redis.opsForValue().set(key, value, timeoutSec, TimeUnit.SECONDS);
        } catch (Exception ignored) { }
    }

    public String get(String key) {
        try {
            return redis.opsForValue().get(key);
        } catch (Exception e) {
            return null;
        }
    }

    public void del(String key) {
        try { redis.delete(key); } catch (Exception ignored) { }
    }
}
