package com.future;

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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
@State(Scope.Benchmark)
public class GrpcFutuerClient {
    private static final String target = System.getProperty("app.target", "localhost:8888");
    private static final int iterations = Integer.parseInt(System.getProperty("app.iterations", "1"));
    private ArrayList<IsoProcessor.BenchmarkMessage> requestList;
    private NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(2);

    @Setup
    public void init() {
        log.error("BM_init:app.target[{}],app.iterations[{}]", target, iterations);
        requestList = new ArrayList<>();
        for (int i = 1; i <= iterations; i++) {
            IsoProcessor.BenchmarkMessage message = IsoProcessor.BenchmarkMessage.newBuilder()
                    .setField1("许多往事在眼前一幕一幕，变的那麼模糊")
                    .setField100(i)
                    .setField101(251).build();
            requestList.add(message);
        }
    }

    @TearDown
    public void end() {
        nioEventLoopGroup.shutdownGracefully();
        log.error("BM_completed!");
    }

    @Benchmark()
    public void runClient() throws InterruptedException {
        log.info("run-client");
        ManagedChannel channel = getChannel();
        HelloGrpc.HelloFutureStub stub = HelloGrpc.newFutureStub(channel);
        CountDownLatch downLatch = new CountDownLatch(iterations);
        for (IsoProcessor.BenchmarkMessage benchmarkMessage : requestList) {
            call(stub, benchmarkMessage, downLatch);
        }
        downLatch.await();
        channel.shutdownNow();
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
}
