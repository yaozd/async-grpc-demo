package org.nuhara.demos;

import com.GrpcUtil;
import com.mattie.grpc.GreeterGrpc;
import com.mattie.grpc.HelloWorldProtos;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class GrpcClientSimpleTest {
    @Rule
    public ContiPerfRule i = new ContiPerfRule();

    private ManagedChannel channel;
    private ManagedChannelBuilder<?> channelBuilder;
    private AtomicInteger num = new AtomicInteger(0);

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
                .disableRetry()
                .usePlaintext();
        channel = channelBuilder.build();
    }

    @After
    @SneakyThrows
    public void end() {
        log.info("[END]Call end().测试完，正常关闭当前连接,成功完成请求数：[{}]",SUCCESS_NUM.get());
        channel.shutdownNow();
        Thread.currentThread().join(2000);
    }

    AtomicInteger I=new AtomicInteger(0);
    AtomicInteger SUCCESS_NUM=new AtomicInteger(0);
    @Test
    //@PerfTest(threads = 10, invocations = 100000)
    //@PerfTest(threads = 100, invocations = 1000000)
    @PerfTest(threads = 100, invocations = 240)
    public void oneCallTest() throws InterruptedException {
        if (I.incrementAndGet()>230) {
            //人为关闭连
            channel.shutdown();
            //channel.shutdownNow();
        }
        GreeterGrpc.GreeterBlockingStub blockingStub = GreeterGrpc.newBlockingStub(channel);
        Map<String, String> header = new HashMap<>();

        //header.put("serviceaccesstoken","xx");
        header.put("serviceaccesstoken", "dbde03c7-de30-4612-bdd5-b85cfe183573");
        header.put("groupid", "11157");
        header.put("shopid", "76072216");
        //header.put("traceid", "fcfbbec0-9a9c-4940-a744-c0569224731f");
        //header.put("X-b3-sampled", "1");
        //header.put("x-b3-traceid", "b3774af5b68decdc");
        //header.put("x-b3-spanid", "a03615e6ad01667b");

        blockingStub = GrpcUtil.attachHeaders(blockingStub, header);
        blockingStub = GrpcUtil.attachHeaders(blockingStub, header);
        try {
            HelloWorldProtos.HelloReply helloReply = blockingStub.
                    sayHello(HelloWorldProtos.HelloRequest.newBuilder().setMessage("hello wolrd").build());
            log.info(helloReply.getMessage());
            SUCCESS_NUM.incrementAndGet();
            //log.info("done!");
            //Thread.currentThread().join();
        } catch (Exception e) {
            log.error("exception", e);
        }
    }

    @Test
    @PerfTest(threads = 100, invocations = 100000)
    //@PerfTest(threads = 100, invocations = 100000)
    public void mutilCallTest() throws InterruptedException {
        ManagedChannel channel = channelBuilder.build();
        GreeterGrpc.GreeterBlockingStub blockingStub = GreeterGrpc.newBlockingStub(channel);
        Map<String, String> header = new HashMap<>();

        //header.put("serviceaccesstoken","xx");
        header.put("serviceaccesstoken", "dbde03c7-de30-4612-bdd5-b85cfe183573" + num.incrementAndGet());
        header.put("groupid", "11157");
        header.put("shopid", "76072216");
        header.put("traceid", "fcfbbec0-9a9c-4940-a744-c0569224731f");
        header.put("version", "20201030(ca83d63d-beta)");
        blockingStub = GrpcUtil.attachHeaders(blockingStub, header);
        blockingStub = GrpcUtil.attachHeaders(blockingStub, header);
        for (int j = 0; j < 100; j++) {
            try {
                HelloWorldProtos.HelloReply helloReply = blockingStub.withDeadlineAfter(50, TimeUnit.MILLISECONDS).
                        sayHello(HelloWorldProtos.HelloRequest.newBuilder().setMessage("hello wolrd").build());
                log.info(helloReply.getMessage());

                log.info("done!");
                //Thread.currentThread().join();
            } catch (Exception e) {
                log.error("exception", e);
            }
        }

    }
}
