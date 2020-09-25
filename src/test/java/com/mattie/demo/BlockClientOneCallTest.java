package com.mattie.demo;

import com.mattie.grpc.GreeterGrpc;
import com.mattie.grpc.HelloWorldProtos;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.yzd.demo.FluentHttpUtil.sendConfig;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class BlockClientOneCallTest {
    @Rule
    public ContiPerfRule i = new ContiPerfRule();

    private ManagedChannel channel;

    @Before
    public void init() {

        sendConfig("/config/protocol/http2.json");
        //
        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .maxInboundMessageSize(1024 * 1024 * 20)
                //发起RST_STREAM 帧（RST_STREAM 类型的 frame，可以在不断开连接的前提下取消某个 request 的 stream）：
                //通过keepAliveTime与keepAliveTimeout的时间调整,可以模拟RST_STREAM 帧
                .keepAliveTime(10, TimeUnit.MINUTES)
                .keepAliveTimeout(10, TimeUnit.MINUTES)
                .idleTimeout(10, TimeUnit.MINUTES)
                .enableFullStreamDecompression()
                .usePlaintext();
        channel = channelBuilder.build();
    }

    @After
    public void end() {
        //模拟：远程主机强迫关闭了一个现有的连接
        channel.shutdownNow();
    }


    @Test
    @PerfTest(threads = 1, invocations = 100000)
    public void oneCallTest() {
        GreeterGrpc.GreeterBlockingStub blockingStub = GreeterGrpc.newBlockingStub(channel);
        HelloWorldProtos.HelloReply helloReply = blockingStub.
                sayHello(HelloWorldProtos.HelloRequest.newBuilder().setMessage("hello wolrd").build());
        log.info(helloReply.getMessage());
    }
}
