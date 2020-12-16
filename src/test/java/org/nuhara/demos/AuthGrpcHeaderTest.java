package org.nuhara.demos;

import com.GrpcUtil;
import com.mattie.grpc.GreeterGrpc;
import com.mattie.grpc.HelloWorldProtos;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class AuthGrpcHeaderTest {
    @Rule
    public ContiPerfRule i = new ContiPerfRule();

    private ManagedChannel channel;
    private ManagedChannelBuilder<?> channelBuilder;

    @Before
    public void init() {

        //sendConfig("/config/protocol/http2.json");
        //
        channelBuilder = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                //.forAddress("localhost", 8899)
                .maxInboundMessageSize(1024 * 1024 * 20)
                //发起RST_STREAM 帧（RST_STREAM 类型的 frame，可以在不断开连接的前提下取消某个 request 的 stream）：
                //通过keepAliveTime与keepAliveTimeout的时间调整,可以模拟RST_STREAM 帧
                //.keepAliveTime(10, TimeUnit.SECONDS)
                //.keepAliveTimeout(10, TimeUnit.SECONDS)
                //.idleTimeout(10, TimeUnit.SECONDS)
                .enableFullStreamDecompression()
                .usePlaintext();
        channel = channelBuilder.build();
    }

    @Test
    //@PerfTest(threads = 10, invocations = 100000)
    //@PerfTest(threads = 100, invocations = 100000)
    public void oneCallTest() throws InterruptedException {
        GreeterGrpc.GreeterBlockingStub blockingStub = GreeterGrpc.newBlockingStub(channel);
        Map<String, String> header = new HashMap<>();

        //header.put("serviceaccesstoken","xx");

        blockingStub = GrpcUtil.attachHeaders(blockingStub, header);
        try {
            HelloWorldProtos.HelloReply helloReply = blockingStub.
                    sayHello(HelloWorldProtos.HelloRequest.newBuilder().setMessage("hello wolrd").build());
            log.info(helloReply.getMessage());

            log.info("done!");
            //Thread.currentThread().join();
        } catch (Exception e) {
            log.error("exception", e);
        }
    }
}
