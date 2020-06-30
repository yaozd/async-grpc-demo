package com.mattie.demo;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.netty.NettyServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class StreamServer {
    /**
     * java '-Dapp.log.level=error' -jar .\stream-server-f06a3c3.jar
     *
     * [gRPC入门-双向流式通信](https://www.jianshu.com/p/323806eb91bb)
     *
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        ServerBuilder<?> serverBuilder = NettyServerBuilder.forPort(8899);
        serverBuilder.addService(new MyService());
        serverBuilder.maxInboundMessageSize(1024 * 1024 * 20);
        serverBuilder.handshakeTimeout(10, TimeUnit.SECONDS);
        Server server = serverBuilder.build();
        server.start();
        broadCast();
        server.awaitTermination();
    }

    private static void broadCast() {
        Thread t = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                try {
                    Thread.sleep(1000 * 3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                MyService.broadCast("hello");
            }
        });
        t.start();
    }
}
