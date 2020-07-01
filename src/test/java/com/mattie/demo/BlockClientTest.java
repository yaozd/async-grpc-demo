package com.mattie.demo;

import com.mattie.grpc.GreeterGrpc;
import com.mattie.grpc.HelloWorldProtos;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class BlockClientTest {
    ManagedChannel channel;
    AtomicInteger counter = new AtomicInteger();

    @Before
    public void init() {
        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder
                .forAddress("localhost", 8888)
                .maxInboundMessageSize(1024 * 1024 * 20)
                .usePlaintext();
        channel = channelBuilder.build();
    }

    /**
     * 测试：多个客户端同时发起请求
     */
    @Test
    public void blockClientTest() {
        int nThreads = 20;
        CompletableFuture[] tasks = new CompletableFuture[nThreads];
        for (int i = 0; i < nThreads; i++) {
            tasks[i] = CompletableFuture.runAsync(this::call, Executors.newCachedThreadPool());
        }
        CompletableFuture<Void> all = CompletableFuture.allOf(tasks);
        //等待所有异步程序处理完成
        all.join();
    }

    public void call() {
        log.info("COUNTER_VALUER:" + counter.incrementAndGet());
        GreeterGrpc.GreeterBlockingStub blockingStub = GreeterGrpc.newBlockingStub(channel);
        HelloWorldProtos.HelloReply helloReply = blockingStub.
                sayHello(HelloWorldProtos.HelloRequest.newBuilder().setMessage("hello wolrd").build());
        log.info(helloReply.getMessage());
    }
}
