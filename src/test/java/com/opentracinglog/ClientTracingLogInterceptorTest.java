package com.opentracinglog;

import com.opentracing.TracedClient;
import lombok.SneakyThrows;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @Author: yaozh
 * @Description:
 */
public class ClientTracingLogInterceptorTest {
    @Rule
    public ContiPerfRule i = new ContiPerfRule();

    @Test
    @SneakyThrows
    @PerfTest(threads = 1, invocations = 10)
    public void tracedLogClientTest() {
        TracingLogClient client = new TracingLogClient("localhost", 50051, new ClientTracingLogInterceptor());
        client.greet("A");
        client.shutdown();
    }
}