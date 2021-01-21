package com.opentracinglog;

import lombok.SneakyThrows;
import org.junit.Test;

/**
 * @Author: yaozh
 * @Description:
 */
public class ServerTracingLogInterceptorTest {
    @Test
    @SneakyThrows
    public void tracedServiceTest() {
        TracingLogServer tracingLogServer = new TracingLogServer();
        tracingLogServer.start();
        tracingLogServer.blockUntilShutdown();
    }

}