package com.benchmark;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.nuhara.model.proto.HelloGrpc;
import org.nuhara.model.proto.IsoProcessor;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
@State(Scope.Benchmark)
public class BenchmarkDemo implements Runnable {
    public static final String target = "localhost:8888";
    private final static int iterations = 1;
    private ArrayList<IsoProcessor.BenchmarkMessage> requestList;
    private NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(2);

    /**
     * 初始化参数
     */
    @Setup
    public void init() {
        requestList = new ArrayList<>();
        for (int i = 1; i <= iterations; i++) {
            IsoProcessor.BenchmarkMessage message = IsoProcessor.BenchmarkMessage.newBuilder()
                    .setField1("许多往事在眼前一幕一幕，变的那麼模糊").setField100(i).setField101(251).build();
            requestList.add(message);
        }
    }

    /**
     * TearDown marks the fixture method to be run after the benchmark.
     */
    @TearDown
    public void end() {
        log.info("Benchmark completed!");
    }

    @Benchmark
    public void m() {
        int nThreads = 1;
        CompletableFuture[] tasks = new CompletableFuture[nThreads];
        for (int i = 0; i < nThreads; i++) {
            tasks[i] = CompletableFuture.runAsync(this::run);
        }
        CompletableFuture<Void> all = CompletableFuture.allOf(tasks);
        //等待所有异步程序处理完成
        all.join();
//        for (CompletableFuture task : tasks) {
//            task.join();
//        }
//        all.cancel(true);
        nioEventLoopGroup.shutdownGracefully();
        log.info("completed");
    }

    private void call(HelloGrpc.HelloFutureStub stub, IsoProcessor.BenchmarkMessage message, CountDownLatch downLatch) {
        ListenableFuture<IsoProcessor.BenchmarkMessage> response = stub.say(message);
        Futures.addCallback(response, new FutureCallback<IsoProcessor.BenchmarkMessage>() {
            @Override
            public void onSuccess(IsoProcessor.BenchmarkMessage result) {
                downLatch.countDown();
            }

            @Override
            public void onFailure(Throwable t) {
                log.warn("Error_on: " + t.getMessage());
                downLatch.countDown();
            }
        });
    }

    private ManagedChannel getChannel() {
        return NettyChannelBuilder.forTarget(target)
                .eventLoopGroup(nioEventLoopGroup)
                .directExecutor()
                .disableRetry()
                .maxInboundMessageSize(20971520)
                .keepAliveTimeout(1, TimeUnit.SECONDS)
                .idleTimeout(1, TimeUnit.SECONDS)
                .usePlaintext().build();
    }

    @Override
    public void run() {
        ManagedChannel channel = getChannel();
        HelloGrpc.HelloFutureStub stub = HelloGrpc.newFutureStub(channel);
        CountDownLatch downLatch = new CountDownLatch(iterations);
        for (IsoProcessor.BenchmarkMessage benchmarkMessage : requestList) {
            call(stub, benchmarkMessage, downLatch);
        }
        try {
            downLatch.await();
        } catch (InterruptedException e) {
            log.error("DownLatch exception!", e);
        }
        channel.shutdown();
        try {
            channel.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Channel exception!", e);
        }
        log.info("Task completed!");
    }
}
