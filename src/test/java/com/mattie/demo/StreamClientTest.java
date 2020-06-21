package com.mattie.demo;

import com.mattie.grpc.GreeterGrpc;
import com.mattie.grpc.HelloWorldProtos;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.Test;

public class StreamClientTest {
    /**
     * 双向流式通信-广播模式测试场景
     */
    @Test
    public void broadCastTest() throws InterruptedException {
        ManagedChannelBuilder<?> channelBuilder =
                ManagedChannelBuilder.forAddress("localhost", 8899).usePlaintext();
        ManagedChannel channel = channelBuilder.build();
        for (int i = 0; i < 100; i++) {
            buildClient(channel);
        }
        Thread.sleep(1000*20);
    }

    public void buildClient(Channel channel) {
        StreamObserver<HelloWorldProtos.HelloStreamRequest> requestObserver =
                GreeterGrpc.newStub(channel).biStream(new StreamObserver<HelloWorldProtos.HelloStreamResponse>() {
                    @Override
                    public void onNext(HelloWorldProtos.HelloStreamResponse value) {
                        System.out.println(value.getResponseInfo());
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });
        requestObserver.onNext(HelloWorldProtos.HelloStreamRequest.newBuilder().setRequestInfo("hello server1").build());
        requestObserver.onNext(HelloWorldProtos.HelloStreamRequest.newBuilder().setRequestInfo("hello server2").build());
        requestObserver.onNext(HelloWorldProtos.HelloStreamRequest.newBuilder().setRequestInfo("hello server3").build());
        requestObserver.onNext(HelloWorldProtos.HelloStreamRequest.newBuilder().setRequestInfo("hello server4").build());
    }

}