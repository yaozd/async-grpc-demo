package org.nuhara.demos;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.channel.nio.NioEventLoopGroup;
import org.junit.Test;
import org.nuhara.model.proto.HelloGrpc;
import org.nuhara.model.proto.IsoProcessor;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Token验证调用模拟
 *
 * @Author: yaozh
 * @Description:
 */
public class AuthGrpcClientMockTest {
    private AtomicInteger unavailableExceptionCount = new AtomicInteger(0);


    @Test
    public void authMockTest() throws InterruptedException {
        //final ManagedChannel channel = NettyChannelBuilder.forTarget("localhost:8888")
        final ManagedChannel channel = NettyChannelBuilder.forTarget("localhost:50051")
                .eventLoopGroup(new NioEventLoopGroup(1))
                .directExecutor()
                //.executor(newThreadPoolExecutor())
                .disableRetry()
                .maxInboundMessageSize(20971520)
                .keepAliveTimeout(1, TimeUnit.SECONDS)
                .idleTimeout(1, TimeUnit.SECONDS)
                .usePlaintext().build();
        IsoProcessor.BenchmarkMessage message = IsoProcessor.BenchmarkMessage.newBuilder()
                .setField1("许多往事在眼前一幕一幕，变的那麼模糊").setField100(1).setField101(251).build();
        HelloGrpc.HelloStub helloStub = HelloGrpc.newStub(channel);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1_000_000; i++) {
            try {
                //TODO 此处会有异常，必须进行Catch
                //io.netty.util.internal.OutOfDirectMemoryError: failed to allocate 16777216 byte(s) of direct memory (used: 3774873887, max: 3786407936)
                //helloStub.say(message, new AutoResponseObserver(i + "test", unavailableExceptionCount));
                helloStub.withDeadlineAfter(2, TimeUnit.SECONDS).say(message, new AutoResponseObserver(i + "test", unavailableExceptionCount));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        long endTime = System.currentTimeMillis();
        //futureStubTest(channel);
        Thread.currentThread().join(100 * 1000);
        System.out.println(endTime - startTime);
    }

    /**
     * 10个连接(channel)
     * 10个stub
     * 随机获取1个。
     *
     * @throws InterruptedException
     */
    @Test
    public void authMockV2Test() throws InterruptedException {
        IsoProcessor.BenchmarkMessage message = IsoProcessor.BenchmarkMessage.newBuilder()
                .setField1("许多往事在眼前一幕一幕，变的那麼模糊").setField100(1).setField101(251).build();
        AuthGrpcClient clientManager = new AuthGrpcClient(10);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1_000_000; i++) {
            try {
                HelloGrpc.HelloStub helloStub = clientManager.getStub();
                //TODO 此处会有异常，必须进行Catch
                //io.netty.util.internal.OutOfDirectMemoryError: failed to allocate 16777216 byte(s) of direct memory (used: 3774873887, max: 3786407936)
                //helloStub.say(message, new AutoResponseObserver(i + "test", unavailableExceptionCount));
                //TODO 具体的超时时间，以项目需求为准。
                helloStub.withDeadlineAfter(2, TimeUnit.SECONDS).say(message, new AutoResponseObserver(i + "test", unavailableExceptionCount));
            } catch (Exception ex) {
                //严重异常
                //io.netty.util.internal.OutOfDirectMemoryError: failed to allocate 16777216 byte(s) of direct memory (used: 3774873887, max: 3786407936)
                ex.printStackTrace();
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);
        //futureStubTest(channel);
        Thread.currentThread().join(10 * 1000);
        System.out.println(endTime - startTime);
    }

    private void futureStubTest(ManagedChannel channel) {
        HelloGrpc.HelloFutureStub stub = HelloGrpc.newFutureStub(channel);
        IsoProcessor.BenchmarkMessage message = IsoProcessor.BenchmarkMessage.newBuilder()
                .setField1("许多往事在眼前一幕一幕，变的那麼模糊").setField100(1).setField101(251).build();
        ListenableFuture<IsoProcessor.BenchmarkMessage> say = stub.say(message);
    }


    private class AutoResponseObserver implements StreamObserver<IsoProcessor.BenchmarkMessage> {
        private final String test;
        private final AtomicInteger unavailableExceptionCount;


        public AutoResponseObserver(String test, AtomicInteger unavailableExceptionCount) {
            this.test = test;
            this.unavailableExceptionCount = unavailableExceptionCount;
        }

        @Override
        public void onNext(IsoProcessor.BenchmarkMessage value) {
            if (unavailableExceptionCount.get() > 0) {
                unavailableExceptionCount.decrementAndGet();
            }
            System.out.println(value);
        }

        @Override
        public void onError(Throwable t) {
            if (t instanceof StatusRuntimeException) {
                StatusRuntimeException statusRuntimeException = (StatusRuntimeException) t;
                //考虑第三方服务发版与网格抖动情况下，前100个请求增加重试逻辑，其他异常不设置重试逻辑
                if (unavailableExceptionCount.get() < 100
                        && statusRuntimeException.getStatus() != null
                        && Status.Code.UNAVAILABLE.equals(statusRuntimeException.getStatus().getCode())) {
                    System.out.println(test + "UNAVAILABLE");
                    unavailableExceptionCount.incrementAndGet();
                    //增加重试逻辑
                }
            }
            //超时异常：io.grpc.StatusRuntimeException: DEADLINE_EXCEEDED: deadline exceeded after 1999997600ns
            //t.printStackTrace();
            //System.out.println(t);
        }

        @Override
        public void onCompleted() {
            System.out.println("completed");
        }
    }
}
