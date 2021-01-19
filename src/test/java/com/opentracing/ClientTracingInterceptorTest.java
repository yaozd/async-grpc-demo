package com.opentracing;


import io.opentracing.mock.MockTracer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class ClientTracingInterceptorTest {
    @Test
    @SneakyThrows
    public void tracedClientTest() {
        TracedClient client = new TracedClient("localhost", 50051, null);
        client.greet("A");
    }
    @Test
    @SneakyThrows
    public void tracedClientWithTracingInterceptorTest() {
        MockTracer clientTracer = new MockTracer();
        ClientTracingInterceptor tracingInterceptor = new ClientTracingInterceptor(clientTracer);
        TracedClient client = new TracedClient("localhost", 50051, tracingInterceptor);
        client.greet("B");
    }
}