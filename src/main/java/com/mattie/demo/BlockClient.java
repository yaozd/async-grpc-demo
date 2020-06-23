package com.mattie.demo;

import com.mattie.grpc.GreeterGrpc;
import com.mattie.grpc.HelloWorldProtos;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * 同步模式
 */
public class BlockClient {
    public static void main(String[] args) {
        //使用usePlaintext，否则使用加密连接
        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder
                .forAddress("localhost", 8899).usePlaintext();
        ManagedChannel channel = channelBuilder.build();
        GreeterGrpc.GreeterBlockingStub blockingStub = GreeterGrpc.newBlockingStub(channel);
        for (int i = 0; i < 10000; i++) {
            HelloWorldProtos.HelloReply helloReply = blockingStub.
                    sayHello(HelloWorldProtos.HelloRequest.newBuilder().setMessage("hello wolrd").build());
            System.out.println(helloReply.getMessage());
        }
        //System.out.println("channel shutdown");
        //模拟：远程主机强迫关闭了一个现有的连接
        //channel.shutdownNow();
    }
}
