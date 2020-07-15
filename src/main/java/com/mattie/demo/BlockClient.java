package com.mattie.demo;

import com.mattie.grpc.GreeterGrpc;
import com.mattie.grpc.HelloWorldProtos;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.NettyChannelBuilder;
import lombok.extern.slf4j.Slf4j;


/**
 * 同步模式
 */
@Slf4j
public class BlockClient {
    public static void main(String[] args) {
        //使用usePlaintext，否则使用加密连接
        NettyChannelBuilder channelBuilder = NettyChannelBuilder
                .forAddress("localhost", 8888)
                .maxInboundMessageSize(1024 * 1024 * 20)
                .disableRetry()
                //MAX: 2^31-1
                .flowControlWindow(2147483647)
                .usePlaintext();
        ManagedChannel channel = channelBuilder
                .intercept(new HeaderClientInterceptor())
                .build();
        GreeterGrpc.GreeterBlockingStub blockingStub = GreeterGrpc.newBlockingStub(channel);
        for (int i = 0; i < 10000000; i++) {
            System.out.println(i);
            try{
                HelloWorldProtos.HelloReply helloReply = blockingStub.
                        //测试大文件传输
                        //sayHello(HelloWorldProtos.HelloRequest.newBuilder().setMessage("hello wolrd"+ DataUtil.getMockData(1024*1024*10)).build());
                                sayHello(HelloWorldProtos.HelloRequest.newBuilder().setMessage("hello wolrd").build());
                //log.info(helloReply.getMessage());
            }catch (Exception e){
                log.error(e.getMessage());
            }

        }
        //log.info("channel shutdown");
        //模拟：远程主机强迫关闭了一个现有的连接
        channel.shutdownNow();
    }
}
