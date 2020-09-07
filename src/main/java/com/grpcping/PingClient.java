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
    private static int port = 1900;
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
        Health.PingResponse ping = grpcPingBlockingStub.ping(Health.PingRequest.newBuilder().build());
        System.out.println(ping);
        channel.shutdownNow();
    }
}
