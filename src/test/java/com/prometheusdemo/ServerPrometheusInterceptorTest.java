package com.prometheusdemo;

import com.opentracinglog.TracingLogServer;
import lombok.SneakyThrows;
import org.junit.Test;

/**
 * @Author: yaozh
 * @Description:
 */
public class ServerPrometheusInterceptorTest {
    @Test
    @SneakyThrows
    public void prometheusServiceTest() {
        TracingLogServer tracingLogServer = new TracingLogServer();
        tracingLogServer.start(new ServerPrometheusInterceptor(new RoutingPrometheusMetrics()));
        tracingLogServer.blockUntilShutdown();
    }
}