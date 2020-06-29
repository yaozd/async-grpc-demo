package com.mattie.demo;

import com.mattie.grpc.GreeterGrpc;
import com.mattie.grpc.HelloWorldProtos;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class StreamClientPerfTest {
    private final static int iterations = 3000;
    public static ManagedChannel channel;
    @Rule
    public ContiPerfRule contiPerfRule = new ContiPerfRule();

    @BeforeClass
    public static void init() {
        //使用usePlaintext，否则使用加密连接
        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder
                .forAddress("localhost", 8888)
                .maxInboundMessageSize(1024 * 1024 * 20)
                .usePlaintext();
        channel = channelBuilder.build();
    }

    @Test
    @PerfTest(threads = 100, invocations = 1000)
    public void blockClientTest() {
        GreeterGrpc.GreeterBlockingStub blockingStub = GreeterGrpc.newBlockingStub(channel);
        for (int i = 0; i < iterations; i++) {
            HelloWorldProtos.HelloReply helloReply = blockingStub.
                    //测试大文件传输
                    //sayHello(HelloWorldProtos.HelloRequest.newBuilder().setMessage("hello wolrd"+ DataUtil.getMockData(1024*1024*10)).build());
                            sayHello(HelloWorldProtos.HelloRequest.newBuilder().setMessage("hello wolrd").build());
            log.info(helloReply.getMessage());
        }
        //log.info("channel shutdown");
        //模拟：远程主机强迫关闭了一个现有的连接
        channel.shutdownNow();
    }
}
