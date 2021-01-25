package com.examples;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class FooServer {
    private int port = 50051;
    private Server server;

    void start() throws IOException, InterruptedException {
        server = ServerBuilder.forPort(port)
                .addService(new FooService())
                .build()
                .start();
        log.info("Start server port[{}]", port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                FooServer.this.stop();
            }
        });
    }

    void startMockError() throws IOException, InterruptedException {
        server = ServerBuilder.forPort(port)
                .addService(new FooMockErrorService())
                .build()
                .start();
        log.info("Start server port[{}]", port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                FooServer.this.stop();
            }
        });
    }

    void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

}
