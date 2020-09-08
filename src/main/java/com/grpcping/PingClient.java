package com.grpcping;

import com.demo.grpcPing.GrpcPingGrpc;
import com.demo.grpcPing.Health;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class PingClient {
    //private static int port = 8899;
    private static int port = 50051;
    //private static int port = 9000;
    private static String host;

    public static void main(String[] args) {
        host = "localhost";
        //使用usePlaintext，否则使用加密连接
        NettyChannelBuilder channelBuilder = NettyChannelBuilder
                .forAddress(host, port)
                .maxInboundMessageSize(1024 * 1024 * 20)
                .disableRetry()
                //MAX: 2^31-1
                .flowControlWindow(2147483647)
                //模拟PING
                //.keepAliveWithoutCalls(true)
                //.keepAliveTime(1, TimeUnit.SECONDS)
                //
                .usePlaintext();
        ManagedChannel channel = channelBuilder.build();
        log.error("Client start! ,host:{};port:{}",host, port);
        GrpcPingGrpc.GrpcPingBlockingStub grpcPingBlockingStub = GrpcPingGrpc.newBlockingStub(channel);
        //
        Health.PingResponse ping = grpcPingBlockingStub.ping(Health.PingRequest.newBuilder()
                .setCode(System.currentTimeMillis()+"")
                .build());
        System.out.println(ping);
        for (int i = 0; i < 10_0000; i++) {
            grpcPingBlockingStub.ping(Health.PingRequest.newBuilder()
                    .setCode(System.currentTimeMillis()+"")
                    .build());
        }
        channel.shutdownNow();
    }
}
