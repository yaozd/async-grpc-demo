package io.grpc.benchmarks.qps;

import com.google.protobuf.ByteString;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.benchmarks.proto.BenchmarkServiceGrpc;
import io.grpc.benchmarks.proto.Messages;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class AsyncClient {
    private static Messages.PayloadType payloadType = Messages.PayloadType.COMPRESSABLE;
    private static int clientPayload = 0;
    private static int serverPayload = clientPayload;
    private static AtomicInteger failCounter = new AtomicInteger();
    private static int nThreads = 30000;
    private static CountDownLatch downLatch = new CountDownLatch(nThreads + 1);
    //private static CountDownLatch downLatch = new CountDownLatch(nThreads);

    /**
     * grpc-连接数测试：1万长连接的是否工作正常
     *
     * @param args
     */
    @SneakyThrows
    public static void main(String[] args) {

        CompletableFuture[] tasks = new CompletableFuture[nThreads];
        for (int i = 0; i < nThreads; i++) {
            tasks[i] = CompletableFuture.runAsync(AsyncClient::newConnection, Executors.newCachedThreadPool());
            if (i % 1000 == 0) {
                Thread.sleep(1000);
            }
        }
        CompletableFuture<Void> all = CompletableFuture.allOf(tasks);
        //等待所有异步程序处理完成
        all.join();
        downLatch.await();
        //downLatch.await(10, TimeUnit.SECONDS);
        log.info("total:{},fail count:{}", nThreads, failCounter.get());
        System.exit(0);
    }

    public static void newConnection() {
        //Channel channel = getChannel("172.20.60.45:30009");
        Channel channel = getChannel("172.20.132.85:8888");
        doUnaryCalls(channel);
    }

    private static void doUnaryCalls(Channel channel) {
        final BenchmarkServiceGrpc.BenchmarkServiceStub stub = BenchmarkServiceGrpc.newStub(channel);
        //
        Messages.SimpleRequest req = newRequest();
        stub.unaryCall(req, new StreamObserver<Messages.SimpleResponse>() {

            @SneakyThrows
            @Override
            public void onNext(Messages.SimpleResponse simpleResponse) {
                log.info("ok");
                downLatch.countDown();
                downLatch.await();
            }

            @Override
            public void onError(Throwable throwable) {
                downLatch.countDown();
                failCounter.incrementAndGet();
                Status status = Status.fromThrowable(throwable);
                System.err.println("Encountered an error in unaryCall. Status is " + status);
                throwable.printStackTrace();
            }

            @Override
            public void onCompleted() {
            }
        });
    }

    private static Messages.SimpleRequest newRequest() {
        ByteString body = ByteString.copyFrom(new byte[clientPayload]);
        Messages.Payload payload = Messages.Payload.newBuilder().setType(payloadType).setBody(body).build();

        return Messages.SimpleRequest.newBuilder()
                .setResponseType(payloadType)
                .setResponseSize(serverPayload)
                .setPayload(payload)
                .build();
    }

    private static ManagedChannel getChannel(String target) {
        return NettyChannelBuilder.forTarget(target)
                .eventLoopGroup(new NioEventLoopGroup(1))
                .directExecutor()
                .disableRetry()
                .flowControlWindow(Integer.MAX_VALUE)
                .maxInboundMessageSize(20971520)
                //channel 长连接时间
                .keepAliveTimeout(1000, TimeUnit.SECONDS)
                //channel 空闲超时时间
                .idleTimeout(1000, TimeUnit.SECONDS)
                .usePlaintext().build();
    }
}
