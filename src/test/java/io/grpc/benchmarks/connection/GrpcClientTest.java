package io.grpc.benchmarks.connection;

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
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class GrpcClientTest {
    private static Messages.PayloadType payloadType = Messages.PayloadType.COMPRESSABLE;
    //private static int clientPayload = 100000000;
    private static int clientPayload = 1;
    private static int serverPayload = clientPayload;

    /**
     * 1个客户端调用
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void doUnaryCallsByOneClient() throws IOException, InterruptedException {
        Channel channel = newChannel("127.0.0.1:50051");
        CountDownLatch downLatch = new CountDownLatch(1);
        doUnaryCalls(channel, downLatch);
        //log.info("channel shutdown");
        //模拟：远程主机强迫关闭了一个现有的连接
        //channel.shutdownNow();
        System.out.println("done!");
        downLatch.await();
    }

    private static void doUnaryCalls(Channel channel, CountDownLatch downLatch) {
        final BenchmarkServiceGrpc.BenchmarkServiceStub stub = BenchmarkServiceGrpc.newStub(channel);
        //
        Messages.SimpleRequest req = newRequest();
        stub.unaryCall(req, new StreamObserver<Messages.SimpleResponse>() {

            @SneakyThrows
            @Override
            public void onNext(Messages.SimpleResponse simpleResponse) {
                downLatch.countDown();
                log.info("ok");
            }

            @Override
            public void onError(Throwable throwable) {
                downLatch.countDown();
                Status status = Status.fromThrowable(throwable);
                System.err.println("Encountered an error in unaryCall. Status is " + status);
                throwable.printStackTrace();
            }

            @Override
            public void onCompleted() {
            }
        });
    }

    private static ManagedChannel newChannel(String target) {
        return NettyChannelBuilder.forTarget(target)
                .eventLoopGroup(new NioEventLoopGroup(1))
                .directExecutor()
                //.disableRetry()
                .keepAliveWithoutCalls(true)
                .keepAliveTime(1000, TimeUnit.MINUTES)
                //.flowControlWindow(Integer.MAX_VALUE)
                //.maxInboundMessageSize(20971520)
                //channel 长连接时间
                .keepAliveTimeout(1000, TimeUnit.MINUTES)
                //channel 空闲超时时间
                .idleTimeout(1000, TimeUnit.MINUTES)
                .usePlaintext().build();
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
}
