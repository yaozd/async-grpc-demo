package com.opentracinglog;

import com.mattie.grpc.GreeterGrpc;
import com.mattie.grpc.HelloWorldProtos;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.nuhara.demos.TokenServerInterceptor;

import java.io.IOException;

@Slf4j
public class TracingLogServer {
    private int port = 50051;
    private Server server;

    void start() throws IOException, InterruptedException {
        server = ServerBuilder.forPort(port)
                .intercept(new TokenServerInterceptor())//增加token
                .intercept(new ServerTracingLogInterceptor())
                .addService(new GreeterImpl())
                .build()
                .start();
        log.info("Start server port[{}]", port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                TracingLogServer.this.stop();
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

    private class GreeterImpl extends GreeterGrpc.GreeterImplBase {
        @Override
        public void sayHello(HelloWorldProtos.HelloRequest req, StreamObserver<HelloWorldProtos.HelloReply> responseObserver) {
            HelloWorldProtos.HelloReply reply = HelloWorldProtos.HelloReply.newBuilder().setMessage("Hello").build();
            //简单模拟人为异常
            //int a=0;
            //int c=1/a;
            //responseObserver.onError(new RuntimeException("test"));
            //
            //返回一个包装成Exception的Status来返回错误信息，如果直接使用Throwable，客户端无法获得错误信息
            //responseObserver.onError(Status.INTERNAL.withDescription("error desc").asRuntimeException());
            //
            responseObserver.onNext(reply);
            responseObserver.onCompleted();

        }
    }
}
