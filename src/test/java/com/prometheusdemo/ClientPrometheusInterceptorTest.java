package com.prometheusdemo;

import com.GrpcUtil;
import com.opentracinglog.TracingLogClient;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class ClientPrometheusInterceptorTest {
    @Rule
    public ContiPerfRule i = new ContiPerfRule();

    @Test
    @SneakyThrows
    @PerfTest(threads = 10, invocations = 100)
    public void clientTest() {
        TracingLogClient client = new TracingLogClient("localhost", 50051);
        Map<String, String> header = new HashMap<>();
        header.put("token", "xxx-xxx-xxxx");
        client.setBlockingStub(GrpcUtil.attachHeaders(client.getBlockingStub(), header));
        client.greet("A");
        client.shutdown();

    }

    @Test
    @SneakyThrows
    @PerfTest(threads = 1, invocations = 10)
    public void prometheusClientTest() {
        RoutingPrometheusMetrics routingPrometheusMetrics = new RoutingPrometheusMetrics();
        TracingLogClient client = new TracingLogClient("localhost", 50051, new ClientPrometheusInterceptor(routingPrometheusMetrics));
        Map<String, String> header = new HashMap<>();
        header.put("token", "xxx-xxx-xxxx");
        //client.setBlockingStub(GrpcUtil.attachHeaders(client.getBlockingStub(), header));
        client.greet("A");
        client.shutdown();

    }

    @Test
    @SneakyThrows
    @PerfTest(threads = 10, invocations = 100)
    public void prometheusClientByExportTest() {
        RoutingPrometheusMetrics routingPrometheusMetrics = RoutingPrometheusExport.getRoutingPrometheusMetrics();
        TracingLogClient client = new TracingLogClient("localhost", 50051, new ClientPrometheusInterceptor(routingPrometheusMetrics));
        Map<String, String> header = new HashMap<>();
        header.put("token", "xxx-xxx-xxxx");
        client.setBlockingStub(GrpcUtil.attachHeaders(client.getBlockingStub(), header));
        client.greet("A");
        client.shutdown();
    }

    //@After
    public void tearDown() throws Throwable {
        CollectorRegistry defaultRegistry = CollectorRegistry.defaultRegistry;
        //CollectorRegistry defaultRegistry = new CollectorRegistry();
        StringWriter writer = new StringWriter();
        TextFormat.write004(writer, defaultRegistry.metricFamilySamples());
        log.info("METRIC:\r\n{}", writer.toString());
    }

}