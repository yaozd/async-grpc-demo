package com.mattie.demo;

import com.mattie.grpc.GreeterGrpc.GreeterImplBase;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import static com.mattie.grpc.HelloWorldProtos.*;

@Slf4j
public class MyService extends GreeterImplBase {
    public static final String EMPTY = "";
    public static final Map<StreamObserver<HelloStreamResponse>, String> clientMap = new ConcurrentHashMap<>();

    /**
     * 广播模式
     *
     * @param message
     */
    public static void broadCast(String message) {
        log.info("CLIENT_SIZE:" + clientMap.size());
        clientMap.forEach(new BiConsumer<StreamObserver<HelloStreamResponse>, String>() {
            @Override
            public void accept(StreamObserver<HelloStreamResponse> helloStreamResponseStreamObserver, String s) {
                helloStreamResponseStreamObserver.onNext(HelloStreamResponse.newBuilder().setResponseInfo("broad cast:" + message).build());
                helloStreamResponseStreamObserver.onCompleted();
            }
        });
    }

    /**
     * 可以测试大请求数据包，如：4M
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        log.info("client sayHello:" + request.getMessage());
        HelloReply helloReply = HelloReply.newBuilder().setMessage("hello client.").build();
        responseObserver.onNext(helloReply);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<HelloStreamRequest> biStream(StreamObserver<HelloStreamResponse> responseObserver) {
        return new StreamObserver<HelloStreamRequest>() {
            @Override
            public void onNext(HelloStreamRequest value) {
                clientMap.putIfAbsent(responseObserver, EMPTY);
                log.info("client says: " + value.getRequestInfo());
                responseObserver.onNext(HelloStreamResponse.newBuilder().setResponseInfo("hello client ：" + value.getRequestInfo()).build());
            }

            @Override
            public void onError(Throwable t) {
                clientMap.remove(responseObserver);
            }

            @Override
            public void onCompleted() {
                clientMap.remove(responseObserver);
                responseObserver.onCompleted();
            }
        };
    }
}

