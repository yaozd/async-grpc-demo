package org.nuhara.demos;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.channel.nio.NioEventLoopGroup;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.nuhara.model.proto.HelloGrpc;
import org.nuhara.model.proto.IsoProcessor;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @Author: yaozh
 * @Description:
 */
public class GrpcClientPerfTest {
    private final static Logger logger = Logger.getLogger(GrpcClientPerfTest.class.getCanonicalName());
    private final static int iterations = 3000;
    @Rule
    public ContiPerfRule contiPerfRule = new ContiPerfRule();

    @BeforeClass
    public static void init() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger logger = loggerContext.getLogger("root");
        logger.setLevel(Level.toLevel("error"));
    }

    @Test
    @PerfTest(threads = 100, invocations = 1000)
    public void clientTest() throws InterruptedException {
        final ManagedChannel channel = NettyChannelBuilder.forTarget("localhost:8888")
                .eventLoopGroup(new NioEventLoopGroup(2))
                .directExecutor()
                .disableRetry()
                .maxInboundMessageSize(20971520)
                .keepAliveTimeout(1, TimeUnit.SECONDS)
                .idleTimeout(1, TimeUnit.SECONDS)
                .usePlaintext().build();
        HelloGrpc.HelloFutureStub stub = HelloGrpc.newFutureStub(channel);
        CountDownLatch downLatch = new CountDownLatch(iterations);
        ArrayList<IsoProcessor.BenchmarkMessage> requestList = new ArrayList<>();
        for (int i = 1; i <= iterations; i++) {
            IsoProcessor.BenchmarkMessage message = IsoProcessor.BenchmarkMessage.newBuilder()
                    .setField1("许多往事在眼前一幕一幕，变的那麼模糊").setField100(i).setField101(251).build();
            requestList.add(message);
        }
        for (IsoProcessor.BenchmarkMessage benchmarkMessage : requestList) {
            call(stub, benchmarkMessage, downLatch);
        }
        downLatch.await();
        channel.shutdown();
        channel.awaitTermination(10,TimeUnit.SECONDS);
    }

    private void call(HelloGrpc.HelloFutureStub stub, IsoProcessor.BenchmarkMessage message, CountDownLatch downLatch) {
        ListenableFuture<IsoProcessor.BenchmarkMessage> response = stub.say(message);
        Futures.addCallback(response, new FutureCallback<IsoProcessor.BenchmarkMessage>() {
            @Override
            public void onSuccess(IsoProcessor.BenchmarkMessage result) {
                downLatch.countDown();
            }

            @Override
            public void onFailure(Throwable t) {
                //logger.warning("Error_on: " + t.getMessage());
                downLatch.countDown();
            }
        });
    }

}
