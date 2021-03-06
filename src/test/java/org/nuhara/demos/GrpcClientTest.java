package org.nuhara.demos;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.yzd.demo.IdCache;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.internal.StringUtil;
import org.databene.contiperf.PerfTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.nuhara.model.proto.HelloGrpc;
import org.nuhara.model.proto.IsoProcessor;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * @Author: yaozh
 * @Description:
 */
public class GrpcClientTest {
    private static CountDownLatch downLatch;
    private final static Logger logger = Logger.getLogger(GrpcClient.class.getCanonicalName());
    private static HelloGrpc.HelloFutureStub stub;
    private final static int iterations = 5000000;
    private static ArrayList<IsoProcessor.BenchmarkMessage> requestList = new ArrayList<>();
    // id 判重使用,避免相同任务重复执行
    private final static Map<String, String> idMap = new ConcurrentHashMap<>();
    public static final String EMPTY = "";

    @BeforeClass
    public static void begin() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger logger = loggerContext.getLogger("root");
        logger.setLevel(Level.toLevel("info"));
        final ManagedChannel channel = NettyChannelBuilder.forTarget("localhost:8888")
                .eventLoopGroup(new NioEventLoopGroup(1))
                .directExecutor()
                //.executor(newThreadPoolExecutor())
                .disableRetry()
                .maxInboundMessageSize(20971520)
                .keepAliveTimeout(1, TimeUnit.SECONDS)
                .idleTimeout(1, TimeUnit.SECONDS)
                .usePlaintext().build();
        stub = HelloGrpc.newFutureStub(channel);
        downLatch = new CountDownLatch(iterations);
        //4MB大小文件
        //byte[] b = new byte[4194304];
        byte[] b = new byte[4194304];
        Arrays.fill(b, (byte)11); //二进制0
        String s = new String(b);
        for (int i = 1; i <= iterations; i++) {

            IsoProcessor.BenchmarkMessage message = IsoProcessor.BenchmarkMessage.newBuilder()
                    .setField1("许多往事在眼前一幕一幕，变的那麼模糊").setField100(i).setField101(251).build();
            requestList.add(message);
        }
    }

    @AfterClass
    public static void end() throws InterruptedException {
        logger.info("Test complete!");
        downLatch.await();
        logger.info("ID_CACHE_SIZE: " + IdCache.getInstance().localCacheSize());
        logger.info("ID_MAP_SIZE: " + idMap.size());
    }

    @Test
    public void clientTest() {
        IsoProcessor.BenchmarkMessage message = IsoProcessor.BenchmarkMessage.newBuilder()
                .setField1("许多往事在眼前一幕一幕，变的那麼模糊").setField100(1).setField101(251).build();
        call(message);
        for (IsoProcessor.BenchmarkMessage benchmarkMessage : requestList) {
            call(benchmarkMessage);
        }
    }

    @Test
    public void multiClientTest() {
        int nThreads = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nThreads; i++) {
            executorService.submit(() -> {
                for (IsoProcessor.BenchmarkMessage benchmarkMessage : requestList) {
                    call(benchmarkMessage);
                }
            });
        }
    }

    private void call(IsoProcessor.BenchmarkMessage message) {
        String idStr = String.valueOf(message.getField100());
        if (StringUtil.isNullOrEmpty(idStr)) {
            return;
        }
        //-添加与判重
        if (idMap.putIfAbsent(idStr, EMPTY) != null) {
            logger.warning("putIfAbsent");
            return;
        }
        ListenableFuture<IsoProcessor.BenchmarkMessage> response = stub.say(message);
        Futures.addCallback(response, new FutureCallback<IsoProcessor.BenchmarkMessage>() {
            @Override
            public void onSuccess(IsoProcessor.BenchmarkMessage result) {
                removeById(idStr);
                Assert.assertTrue(message.getField100() == result.getField100());
                logger.info("Call_Complete: " + result.getField100());
                IdCache.getInstance().put(idStr, "true");
                finish();
            }

            @Override
            public void onFailure(Throwable t) {
                removeById(idStr);
                logger.warning("Error_on: " + t.getMessage());
                finish();
            }
        });

    }

    private void finish() {
        downLatch.countDown();
    }

    private void removeById(String idStr) {
        idMap.remove(idStr);
    }

    /**
     * 参考：FixedThreadPool
     * 线程池之ThreadPoolExecutor使用
     * https://www.jianshu.com/p/f030aa5d7a28
     *
     * @return
     */
    private static Executor newThreadPoolExecutor() {
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), new ThreadFactory() {
            private final AtomicInteger mThreadNum = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "my-thread-" + mThreadNum.getAndIncrement());
            }
        });
    }
}
