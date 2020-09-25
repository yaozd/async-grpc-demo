package com.yzd.demo;

import com.alibaba.dcm.DnsCacheManipulator;

import java.net.InetAddress;

/**
 * @Author: yaozh
 * @Description:
 */
public class DnsUtil {
    public static void loadDnsCache(){
        DnsCacheManipulator.setDnsCache("www.hello.com", "192.168.1.1");
        DnsCacheManipulator.setDnsCache("www.world.com", "1234:5678:0:0:0:0:0:200e");
    }
}
