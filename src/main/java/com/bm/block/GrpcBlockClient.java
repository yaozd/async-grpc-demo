package com.bm.block;

import com.mattie.grpc.GreeterGrpc;
import com.mattie.grpc.HelloWorldProtos;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
//Scope.Thread	默认状态。实例将分配给运行给定测试的每个线程。
@State(Scope.Thread)
public class GrpcBlockClient {
    private NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(2);
    private ManagedChannel channel = getChannel();

    @Setup
    public void init() {
        log.error("BM_init:app.target[{}]", GrpcBlockClientRunner.target);
    }

    @TearDown
    public void end() {
        channel.shutdownNow();
        nioEventLoopGroup.shutdownGracefully();
        log.error("BM_completed!");
    }

    @Benchmark()
    public void runClient() throws InterruptedException {
        log.info("run-client");
        GreeterGrpc.GreeterBlockingStub blockingStub = GreeterGrpc.newBlockingStub(channel);
        HelloWorldProtos.HelloReply helloReply = blockingStub.
                sayHello(HelloWorldProtos.HelloRequest.newBuilder().setMessage("hello wolrd").build());
        log.info(helloReply.getMessage());
    }

    private ManagedChannel getChannel() {
        return NettyChannelBuilder.forTarget(GrpcBlockClientRunner.target)
                .eventLoopGroup(nioEventLoopGroup)
                .directExecutor()
                .disableRetry()
                .maxInboundMessageSize(20971520)
                .keepAliveTimeout(1, TimeUnit.SECONDS)
                .idleTimeout(1, TimeUnit.SECONDS)
                .usePlaintext().build();
    }
}
