package com.opentracinglog;

import com.mattie.grpc.GreeterGrpc;
import com.mattie.grpc.HelloWorldProtos;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class TracingLogClient {
    private final ManagedChannel channel;
    @Getter
    @Setter
    private GreeterGrpc.GreeterBlockingStub blockingStub;

    public TracingLogClient(String host, int port, ClientInterceptor clientInterceptor) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .intercept(clientInterceptor)
                .usePlaintext()
                .build();
        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    public TracingLogClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    public void shutdown() {
        channel.shutdown();
    }


    @SneakyThrows
    public boolean greet(String name) {
        HelloWorldProtos.HelloRequest request = HelloWorldProtos.HelloRequest.newBuilder().setMessage(name).build();
        //模拟操作：客户端设置超时，超时后会触发服务器端的Cancel操作
        //HelloWorldProtos.HelloReply helloReply = blockingStub.withDeadlineAfter(5, TimeUnit.SECONDS).sayHello(request);
        HelloWorldProtos.HelloReply helloReply = blockingStub.sayHello(request);
        log.info("Client receive message [{}]", helloReply.getMessage());
        return true;
    }
}
