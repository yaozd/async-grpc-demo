package com.opentracing;


import io.opentracing.mock.MockTracer;
import lombok.SneakyThrows;
import org.junit.Test;

/**
 * @Author: yaozh
 * @Description:
 */

public class ServerTracingInterceptorTest {

    @Test
    @SneakyThrows
    public void tracedServiceTest() {
        TracedService tracedService = new TracedService();
        tracedService.start();
        tracedService.blockUntilShutdown();
    }

    @Test
    @SneakyThrows
    public void tracedServiceWithTracingInterceptorTest() {
        TracedService tracedService = new TracedService();
        MockTracer serviceTracer = new MockTracer();
        ServerTracingInterceptor tracingInterceptor = new ServerTracingInterceptor.Builder(serviceTracer)
                .withVerbosity()
                .build();;
        tracedService.startWithInterceptor(tracingInterceptor);
        tracedService.blockUntilShutdown();
    }
}
