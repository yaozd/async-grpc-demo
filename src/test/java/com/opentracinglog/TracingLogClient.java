package com.opentracinglog;

import com.mattie.grpc.GreeterGrpc;
import com.mattie.grpc.HelloWorldProtos;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

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

    public TracingLogClient(String host, int port, ClientTracingLogInterceptor tracingLogInterceptor) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .intercept(tracingLogInterceptor)
                .usePlaintext()
                .build();
        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    void shutdown() {
        channel.shutdown();
    }


    @SneakyThrows
    boolean greet(String name) {
        HelloWorldProtos.HelloRequest request = HelloWorldProtos.HelloRequest.newBuilder().setMessage(name).build();
        HelloWorldProtos.HelloReply helloReply = blockingStub.sayHello(request);
        log.info("Client receive message [{}]", helloReply.getMessage());
        return true;
    }
}
