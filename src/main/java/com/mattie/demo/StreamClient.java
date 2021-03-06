package com.mattie.demo;

import com.DataUtil;
import com.mattie.grpc.GreeterGrpc;
import com.mattie.grpc.HelloWorldProtos;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StreamClient {
    public static void main(String[] args) {
        //使用usePlaintext，否则使用加密连接
        ManagedChannelBuilder<?> channelBuilder =
                //ManagedChannelBuilder.forAddress("localhost", 8899).usePlaintext();
                ManagedChannelBuilder.forAddress("localhost", 8888).maxInboundMessageSize(1024 * 1024 * 20).usePlaintext();
        ManagedChannel channel = channelBuilder.build();

        //双向流式通信
        StreamObserver<HelloWorldProtos.HelloStreamRequest> requestObserver =
                GreeterGrpc.newStub(channel).biStream(new StreamObserver<HelloWorldProtos.HelloStreamResponse>() {
                    @Override
                    public void onNext(HelloWorldProtos.HelloStreamResponse value) {
                        log.info(value.getResponseInfo());
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error("error!",t);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
        requestObserver.onNext(HelloWorldProtos.HelloStreamRequest.newBuilder().setRequestInfo("hello server1").build());
        requestObserver.onNext(HelloWorldProtos.HelloStreamRequest.newBuilder().setRequestInfo("hello server2").build());
        requestObserver.onNext(HelloWorldProtos.HelloStreamRequest.newBuilder().setRequestInfo("hello server3"+ DataUtil.getMockData(1024*1024*5)).build());
        requestObserver.onNext(HelloWorldProtos.HelloStreamRequest.newBuilder().setRequestInfo("hello server4"+ DataUtil.getMockData(1024*1024*15)).build());
        //模拟双向流模式，通信过程中单个信息体过大场景
        //requestObserver.onNext(HelloWorldProtos.HelloStreamRequest.newBuilder().setRequestInfo("hello server4"+ DataUtil.getMockData(1024*1024*50)).build());
        requestObserver.onNext(HelloWorldProtos.HelloStreamRequest.newBuilder().setRequestInfo("hello server4").build());
        requestObserver.onNext(HelloWorldProtos.HelloStreamRequest.newBuilder().setRequestInfo("hello server4").build());
        requestObserver.onNext(HelloWorldProtos.HelloStreamRequest.newBuilder().setRequestInfo("hello server4").build());
        requestObserver.onNext(HelloWorldProtos.HelloStreamRequest.newBuilder().setRequestInfo("hello server4").build());
        requestObserver.onNext(HelloWorldProtos.HelloStreamRequest.newBuilder().setRequestInfo("hello server4").build());
        requestObserver.onNext(HelloWorldProtos.HelloStreamRequest.newBuilder().setRequestInfo("hello server4").build());
        requestObserver.onNext(HelloWorldProtos.HelloStreamRequest.newBuilder().setRequestInfo("hello server4").build());
        requestObserver.onNext(HelloWorldProtos.HelloStreamRequest.newBuilder().setRequestInfo("hello server4").build());
        requestObserver.onNext(HelloWorldProtos.HelloStreamRequest.newBuilder().setRequestInfo("hello server4").build());
        requestObserver.onCompleted();
        try {
            Thread.sleep(1000 * 20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //log.info("channel shutdown");
        //模拟：远程主机强迫关闭了一个现有的连接
        //channel.shutdownNow();
    }
}
