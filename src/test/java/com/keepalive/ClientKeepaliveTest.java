package com.keepalive;

import cn.hutool.core.thread.ThreadUtil;
import com.echo.grpc.EchoGreeterGrpc;
import com.echo.grpc.EchoGreeterProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class ClientKeepaliveTest extends AbstractKeepaliveTest {
    private ManagedChannel channel;

    @Before
    public void init() {
        channel = ManagedChannelBuilder
                .forAddress("127.0.0.1", port)
                .forAddress("127.0.0.1", port)
                .forAddress("127.0.0.1", 50051)
                /**
                 * 代表：空闲模式也要触发ping请求
                 * 设置当连接上没有未完成的RPC时是否执行keepalive。
                 * *默认为{@code false}。
                 */
                .keepAliveWithoutCalls(true)
                /**
                 * 空闲模式与非空闲模式（有请求正在处理）：触发
                 * 如果长时间没有收到读时，则发送ping来判断当前连接是否存活。
                 * 默认为20秒，最小为10秒
                 * 如果keepAliveTimeout(1, TimeUnit.SECONDS)则设置为最小值10秒，
                 * 如果客户端频发发送ping请求，则出发too_many_pings
                 */
                .keepAliveTime(5, TimeUnit.SECONDS)
                /**
                 * keepAliveTimeout设置决定触发pingTimeout方法（PS:UNAVAILABLE:Keepalive failed. The connection is likely gone）的速度
                 * 默认为：10秒,最小值为10毫秒
                 */
                .keepAliveTimeout(1, TimeUnit.NANOSECONDS)
                //.keepAliveTimeout(10, TimeUnit.SECONDS)
                .usePlaintext()
                .build();
    }

    @After
    @SneakyThrows
    public void end() {
        log.info("[END]Call end().");
        Thread.currentThread().join();
        channel.shutdownNow();
    }

    /**
     * 此方法无法触发：io.grpc.StatusRuntimeException: UNAVAILABLE:Keepalive failed. The connection is likely gone
     */
    @Test
    @SneakyThrows
    public void keepaliveTest() {
        EchoGreeterGrpc.EchoGreeterBlockingStub blockingStub = EchoGreeterGrpc.newBlockingStub(channel);
        EchoGreeterProto.EchoRequest echoRequest = EchoGreeterProto.EchoRequest.newBuilder()
                .setSize(10)
                .build();
        try {
            EchoGreeterProto.EchoReply echoReply = blockingStub.sayHello(echoRequest);
            log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
        } catch (Exception ex) {
            log.error("", ex);
        }
    }

    /**
     * 问题：io.grpc.StatusRuntimeException: UNAVAILABLE:Keepalive failed. The connection is likely gone
     */
    @Test
    public void keepaliveForKeepaliveFailedTest() {
        EchoGreeterGrpc.EchoGreeterBlockingStub blockingStub = EchoGreeterGrpc.newBlockingStub(channel);
        EchoGreeterProto.EchoRequest echoRequest = EchoGreeterProto.EchoRequest.newBuilder()
                .setSize(10)
                .build();
        EchoGreeterProto.EchoReply echoReply = null;
        try {
            //模拟同时有多个线程在请求服务端
            ThreadUtil.newSingleExecutor().execute(this::AsyncCallWithSleep);
            echoReply = blockingStub.sayHello(echoRequest);
            log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
            ThreadUtil.sleep(60, TimeUnit.SECONDS);
            echoReply = blockingStub.sayHello(echoRequest);
            log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
            ThreadUtil.sleep(60, TimeUnit.SECONDS);
            echoReply = blockingStub.sayHello(echoRequest);
            log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
        } catch (Exception ex) {
            log.error("[T1]Occur Exception!", ex);
        }
        try {
            echoReply = blockingStub.sayHello(echoRequest);
            log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
            ThreadUtil.sleep(60, TimeUnit.SECONDS);
            echoReply = blockingStub.sayHello(echoRequest);
            log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
            ThreadUtil.sleep(60, TimeUnit.SECONDS);
            echoReply = blockingStub.sayHello(echoRequest);
            log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
        } catch (Exception ex) {
            log.error("[T2]Occur Exception!", ex);
        }
    }

    /**
     * 测试流程：
     * 1.启动{@link com.keepalive.ServerKeepaliveTest#keepaliveWithExecutorTest} 方法，此时server是Executor的模式
     */
    @Test
    public void keepaliveForKeepaliveFailedByConcurrentModeTest() {
        EchoGreeterGrpc.EchoGreeterBlockingStub blockingStub = EchoGreeterGrpc.newBlockingStub(channel);
        EchoGreeterProto.EchoRequest echoRequest = EchoGreeterProto.EchoRequest.newBuilder()
                .setSize(10)
                .build();
        EchoGreeterProto.EchoReply echoReply = null;
        try {
            //模拟同时有多个线程在请求服务端
            //模拟并发模式
            for (int i = 0; i < 100; i++) {
                ThreadUtil.newSingleExecutor().execute(this::AsyncCallWithSleep);
            }
            ThreadUtil.newSingleExecutor().execute(this::AsyncCallWithSleep);
            echoReply = blockingStub.sayHello(echoRequest);
            log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
            ThreadUtil.sleep(60, TimeUnit.SECONDS);
            echoReply = blockingStub.sayHello(echoRequest);
            log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
            ThreadUtil.sleep(60, TimeUnit.SECONDS);
            echoReply = blockingStub.sayHello(echoRequest);
            log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
        } catch (Exception ex) {
            log.error("[T1]Occur Exception!", ex);
        }
        try {
            echoReply = blockingStub.sayHello(echoRequest);
            log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
            ThreadUtil.sleep(60, TimeUnit.SECONDS);
            echoReply = blockingStub.sayHello(echoRequest);
            log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
            ThreadUtil.sleep(60, TimeUnit.SECONDS);
            echoReply = blockingStub.sayHello(echoRequest);
            log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
        } catch (Exception ex) {
            log.error("[T2]Occur Exception!", ex);
        }
    }

    public void AsyncCallWithSleep() {
        log.info("[AsyncCallWithSleep]Start!");
        ThreadUtil.sleep(60, TimeUnit.SECONDS);
        EchoGreeterGrpc.EchoGreeterBlockingStub blockingStub = EchoGreeterGrpc.newBlockingStub(channel);
        EchoGreeterProto.EchoRequest echoRequest = EchoGreeterProto.EchoRequest.newBuilder()
                .setSize(10)
                .build();
        EchoGreeterProto.EchoReply echoReply = null;
        try {
            echoReply = blockingStub.sayHello(echoRequest);
            log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
            ThreadUtil.newSingleExecutor().execute(this::AsyncCallWithSleep);
            ThreadUtil.sleep(60, TimeUnit.SECONDS);
            echoReply = blockingStub.sayHello(echoRequest);
            log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
        } catch (Exception ex) {
            log.error("[T1]Occur Exception!", ex);
        }
    }

    /**
     * 通过数学公式的方法来讲解问题：y=(x+z+c)+t;
     * <p>
     * 场景：客户端与后端服务之间的网络不通过。连接超时时间过长造成。
     * 方法检测的PING操作产生的UNAVAILABLE:Keepalive failed. The connection is likely gone异常，在UNAVAILABLE:io exception|UNAVAILABLE:connection timeout之间触发。
     * 所以提示错误为UNAVAILABLE:Keepalive failed. The connection is likely gone。
     * =======================================================================
     * 影响连接超时时间时长的因素有哪些？
     * 1.Netty的ChannelOption.CONNECT_TIMEOUT_MILLIS （PS:Netty参数，连接超时毫秒数，默认值30000毫秒即30秒。）
     * 2.linux系统参数：net.ipv4.tcp_syn_retries(PS:net.ipv4.tcp_syn_retries = 6，默认值6次)
     * 此时客户端发起syn_retries，TCP重试操作
     * TCP重试的次数与linux系统参数：net.ipv4.tcp_syn_retries有关
     * <p>
     * 参考：
     * 理解net.ipv4.tcp_syn_retries设置
     * https://www.dazhuanlan.com/2019/10/20/5dab43fbaadb1/
     */
    @Test
    public void keepaliveForKeepaliveFailedByFutureStubTest() {
        EchoGreeterGrpc.EchoGreeterFutureStub greeterFutureStub = EchoGreeterGrpc.newFutureStub(channel);
        EchoGreeterProto.EchoRequest request = EchoGreeterProto.EchoRequest.newBuilder().setSize(1).build();
        log.info("Call sayHello use future stub");
        for (int i = 0; i < 100; i++) {
            log.info("I=[{}]", i);
            try {
                EchoGreeterProto.EchoReply echoReply = greeterFutureStub.sayHello(request).get(5, TimeUnit.SECONDS);
                log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
            } catch (Exception ex) {
                log.error("", ex);
            }
        }
        //ThreadUtil.sleep(60, TimeUnit.SECONDS);
    }
}
