package com.opentracinglog;

import com.GrpcUtil;
import lombok.SneakyThrows;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: yaozh
 * @Description:
 */
public class ClientTracingLogInterceptorTest {
    @Rule
    public ContiPerfRule i = new ContiPerfRule();

    @Test
    @SneakyThrows
    //@PerfTest(threads = 10, invocations = 100)
    public void tracedLogClientTest() {
        TracingLogClient client = new TracingLogClient("localhost", 50051, new ClientTracingLogInterceptor());
        Map<String, String> header = new HashMap<>();
        header.put("token", "xxx-xxx-xxxx");
        client.setBlockingStub(GrpcUtil.attachHeaders(client.getBlockingStub(), header));
        client.greet("A");
        client.shutdown();
    }
}