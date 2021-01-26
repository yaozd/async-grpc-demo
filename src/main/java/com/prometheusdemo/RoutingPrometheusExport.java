package com.prometheusdemo;

/**
 * 创建一个单例模式
 *
 * @Author: yaozh
 * @Description:
 */
public class RoutingPrometheusExport {

    private static RoutingPrometheusMetrics routingPrometheusMetrics;

    static {
        routingPrometheusMetrics = new RoutingPrometheusMetrics();
    }

    private RoutingPrometheusExport() {
    }

    public static RoutingPrometheusMetrics getRoutingPrometheusMetrics() {
        return routingPrometheusMetrics;
    }
}
