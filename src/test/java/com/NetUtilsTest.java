package com;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class NetUtilsTest {

    @Test
    public void getHostName() {
        log.info(NetUtils.getHostName());
    }

    @Test
    public void getLocalIp() {
        log.info(NetUtils.getLocalIp());
    }
}