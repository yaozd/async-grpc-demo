package com.yzd.demo;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

/**
 * @Author: yaozh
 * @Description:
 */
public class IdCache {
    private static volatile IdCache mInstance;
    private Cache<String, Object> localCache;

    private IdCache() {
        localCache = CacheBuilder.newBuilder()
                // ID缓存时间为固定时长
                // 缓存20分钟(时间起点：entry的创建或替换（即修改）)
                .expireAfterWrite(20, TimeUnit.MINUTES)
                // 缓存10分钟(时间起点：entry的创建或替换（即修改）或最后一次访问)
                //.expireAfterAccess(10, TimeUnit.MINUTES)
                // 最多缓存1000个对象
                .maximumSize(1000)
                .build();
    }

    public static IdCache getInstance() {
        if (mInstance == null) {
            synchronized (IdCache.class) {
                if (mInstance == null) {
                    mInstance = new IdCache();
                }
            }
        }
        return mInstance;
    }

    public String get(Object key) {
        Object value = localCache.getIfPresent(key);
        return value == null ? null : String.valueOf(localCache.getIfPresent(key));
    }

    public void put(String key, Object value) {
        localCache.put(key, value);
    }

    public long localCacheSize() {
        return localCache.size();
    }
}
