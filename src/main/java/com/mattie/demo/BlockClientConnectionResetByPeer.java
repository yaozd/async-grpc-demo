package com.mattie.demo;

import com.mattie.grpc.GreeterGrpc;
import com.mattie.grpc.HelloWorldProtos;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 远程强制关闭
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class BlockClientConnectionResetByPeer {
    //远程强制关闭：Connection reset by peer
    public static void main(String[] args) throws InterruptedException, IOException {
        //使用usePlaintext，否则使用加密连接
        NettyChannelBuilder channelBuilder = NettyChannelBuilder
                //.forAddress("172.20.132.85", 50051)
                //.forAddress("172.20.107.204", 50051)
                //.forAddress("localhost", 8888)
                .forAddress("localhost", 50051)
                //.forAddress("localhost", 80)
                //.forTarget("http2.test.hualala:50051")
                //.forTarget("dohko.grpcproxy.hualala.com:50051")
                //.forTarget("106.2.20.40:80")
                //.forAddress("localhost", 8899)
                .maxInboundMessageSize(1024 * 1024 * 20)
                .disableRetry()
                //MAX: 2^31-1
                .flowControlWindow(2147483647)
                //模拟PING
                //.keepAliveWithoutCalls(true)
                //.keepAliveTime(1, TimeUnit.SECONDS)
                //
                .usePlaintext();
        ManagedChannel channel = channelBuilder
                .intercept(new HeaderClientInterceptor())
                .build();
        channel.resetConnectBackoff();
        GreeterGrpc.GreeterBlockingStub blockingStub = GreeterGrpc.newBlockingStub(channel);
        try {
            HelloWorldProtos.HelloReply helloReply = blockingStub.
                    //测试大文件传输
                    //sayHello(HelloWorldProtos.HelloRequest.newBuilder().setMessage("hello wolrd"+ DataUtil.getMockData(1024*1024*10)).build());
                            sayHello(HelloWorldProtos.HelloRequest.newBuilder().setMessage("hello wolrd").build());
        }catch (Exception e){
            e.printStackTrace();
        }
        //log.info("channel shutdown");
        //模拟：远程主机强迫关闭了一个现有的连接
        //channel.shutdownNow();
        System.out.println("done!");
        //模拟：等待状态！
        Thread.currentThread().join();
    }
}
