package com.prometheusdemo;

import com.opentracinglog.TracingLogServer;
import io.prometheus.client.exporter.HTTPServer;
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
        int port = 1234;
        HTTPServer server = new HTTPServer(port);
        log.info("Prometheus http server port: {}", port);
        log.info("Open the url: http://localhost:{}}/", port);
        //
        TracingLogServer tracingLogServer = new TracingLogServer();
        //tracingLogServer.start(new ServerPrometheusInterceptor(new RoutingPrometheusMetrics()));
        tracingLogServer.start(new ServerPrometheusInterceptor(RoutingPrometheusExport.getRoutingPrometheusMetrics()));
        tracingLogServer.blockUntilShutdown();
    }

    /**
     * 参考：
     * https://github.com/prometheus/client_java#http
     * 打开：
     * http://localhost:1234/
     * 线程数：5个线程
     * this.executorService = Executors.newFixedThreadPool(5, HTTPServer.NamedDaemonThreadFactory.defaultThreadFactory(daemon));
     */
    @Test
    @SneakyThrows
    public void httpServerTests() {
        int port = 1234;
        HTTPServer server = new HTTPServer(port);
        //server.stop();
        Thread.currentThread().join();

    }
}