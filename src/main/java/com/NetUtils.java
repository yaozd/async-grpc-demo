package com;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Author: yaozh
 * @Description:
 */
public class NetUtils {
    static InetAddress addr;

    static
    {
        try
        {
            addr = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 获取主机名
     *
     * @return
     */
    public static String getHostName()
    {
        if (null != addr)
        {
            return addr.getHostName();
        }
        return null;
    }

    /**
     * 获取本机IP
     *
     * @return
     */
    public static String getLocalIp()
    {
        if (null != addr)
        {
            return addr.getHostAddress();
        }
        return null;
    }
}
