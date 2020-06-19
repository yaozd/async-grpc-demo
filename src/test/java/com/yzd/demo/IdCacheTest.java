package com.yzd.demo;

import org.junit.Test;

import java.util.logging.Logger;

/**
 * @Author: yaozh
 * @Description:
 */
public class IdCacheTest {
    private final static Logger logger = Logger.getLogger(IdCacheTest.class.getCanonicalName());

    @Test
    public void putAndGet() {
        String key = "key";
        String value = IdCache.getInstance().get(key);
        logger.info(value);
        IdCache.getInstance().put(key, "value-01");
        value = IdCache.getInstance().get(key);
        logger.info(value);
    }
}
