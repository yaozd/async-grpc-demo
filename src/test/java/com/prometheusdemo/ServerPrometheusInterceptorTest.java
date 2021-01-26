package com.prometheusdemo;

import com.opentracinglog.TracingLogServer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class ServerPrometheusInterceptorTest {
    @Test
    @SneakyThrows
    public void prometheusServiceTest() {
        TracingLogServer tracingLogServer = new TracingLogServer();
        tracingLogServer.start(new ServerPrometheusInterceptor(new RoutingPrometheusMetrics()));
        tracingLogServer.blockUntilShutdown();
    }
}