package com.grpcping;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class PingServer {

    private static int port = 1900;

    public static void main(String[] args) throws IOException, InterruptedException {
        NettyServerBuilder serverBuilder = NettyServerBuilder.forPort(port);
        serverBuilder.addService(new PingService());
        //serverBuilder.flowControlWindow(1000000000);
        //模拟PING
        //serverBuilder.permitKeepAliveWithoutCalls(true);
        //serverBuilder.keepAliveTime(3,TimeUnit.SECONDS);
        //PING
        serverBuilder.maxInboundMessageSize(1024 * 1024 * 20);
        serverBuilder.handshakeTimeout(10, TimeUnit.SECONDS);
        Server server = serverBuilder.build();
        server.start();
        log.error("Server start! ,port:{}", port);
        Thread.currentThread().join();
    }

}
