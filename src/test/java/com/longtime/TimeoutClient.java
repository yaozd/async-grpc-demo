package com.longtime;

import com.echo.grpc.EchoGreeterGrpc;
import com.echo.grpc.EchoGreeterProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class TimeoutClient {
    @Rule
    public ContiPerfRule i = new ContiPerfRule();

    private ManagedChannel channel;

    @Before
    public void init() {
        channel = ManagedChannelBuilder
                .forAddress("127.0.0.1", 50051)
                .usePlaintext()
                .build();
    }

    @After
    @SneakyThrows
    public void end() {
        log.info("[END]Call end().");
        Thread.currentThread().join();
        channel.shutdownNow();
    }

    /**
     * 模拟耗时请求，触发网关timeout
     */
    @Test
    @PerfTest(threads = 10, invocations = 10)
    @SneakyThrows
    public void timeoutTest() {
        EchoGreeterGrpc.EchoGreeterBlockingStub blockingStub = EchoGreeterGrpc.newBlockingStub(channel);
        EchoGreeterProto.EchoRequest echoRequest = EchoGreeterProto.EchoRequest.newBuilder()
                .setSleepMills(70 * 1000)
                .setSize(10)
                .build();
        try {
            EchoGreeterProto.EchoReply echoReply = blockingStub.sayHello(echoRequest);
            log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
        } catch (Exception ex) {
            log.error("", ex);
        }
    }
}
