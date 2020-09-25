package com.yzd.demo;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Java Dns Cache Manipulator
 * DNS（实际上设置的是DNS Cache）
 * https://www.oschina.net/p/java-dns-cache-manipulator
 * @Author: yaozh
 * @Description:
 */

public class DnsUtilTest {
    @Test
    public void loadDnsCache() throws UnknownHostException {
        DnsUtil.loadDnsCache();
        // 支持IPv6地址
        // 之后Java代码中使用到域名都会解析成上面指定的IP。
        // 下面是一个简单获取域名对应的IP，演示一下：
        String ip = InetAddress.getByName("www.hello.com").getHostAddress(); // ip = "192.168.1.1"
        String ipv6 = InetAddress.getByName("www.world.com").getHostAddress(); // ipv6 = "1234:5678:0:0:0:0:0:200e"
        System.out.println(ip);
        System.out.println(ipv6);
    }
}
