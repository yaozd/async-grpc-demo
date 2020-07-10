package com.mattie.demo;

import com.mattie.grpc.GreeterGrpc;
import com.mattie.grpc.HelloWorldProtos;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
                //发起RST_STREAM 帧（RST_STREAM 类型的 frame，可以在不断开连接的前提下取消某个 request 的 stream）：
                //通过keepAliveTime与keepAliveTimeout的时间调整,可以模拟RST_STREAM 帧
                .keepAliveTime(10, TimeUnit.MINUTES)
                .keepAliveTimeout(10, TimeUnit.MINUTES)
                .idleTimeout(10,TimeUnit.MINUTES)
                .usePlaintext();
        channel = channelBuilder.build();
    }
    @After
    public void end(){
        //channel.shutdownNow();
    }

    /**
     * 测试：多个客户端同时发起请求
     */
    @Test
    public void blockClientTest() throws InterruptedException {
        int nThreads = 20;
        CompletableFuture[] tasks = new CompletableFuture[nThreads];
        for (int i = 0; i < nThreads; i++) {
            tasks[i] = CompletableFuture.runAsync(this::call, Executors.newCachedThreadPool());
        }
        CompletableFuture<Void> all = CompletableFuture.allOf(tasks);
        //Thread.sleep(5000);
        //等待所有异步程序处理完成
        all.join();
    }

    public void call() {
        try{
            log.info("COUNTER_VALUER:" + counter.incrementAndGet());
            GreeterGrpc.GreeterBlockingStub blockingStub = GreeterGrpc.newBlockingStub(channel);
            HelloWorldProtos.HelloReply helloReply = blockingStub.
                    sayHello(HelloWorldProtos.HelloRequest.newBuilder().setMessage("hello wolrd").build());
            log.info(helloReply.getMessage());
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
