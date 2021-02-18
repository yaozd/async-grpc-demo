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
                /**
                 * 代表：空闲模式也要触发ping请求
                 * 设置当连接上没有未完成的RPC时是否执行keepalive。
                 * *默认为{@code false}。
                 */
                //.keepAliveWithoutCalls(true)
                /**
                 * 空闲模式与非空闲模式（有请求正在处理）：触发
                 * 如果长时间没有收到读时，则发送ping来判断当前连接是否存活。
                 * 默认为20秒，最小为10秒
                 * 如果keepAliveTimeout(1, TimeUnit.SECONDS)则设置为最小值10秒，
                 * 如果客户端频发发送ping请求，则出发too_many_pings
                 */
                .keepAliveTime(5, TimeUnit.SECONDS)
                /**
                 * keepAliveTimeout设置决定触发pingTimeout方法（PS:）的速度
                 * 默认为：10秒,最小值为10毫秒
                 */
                .keepAliveTimeout(10, TimeUnit.SECONDS)
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
    public void keepaliveTest() {
        EchoGreeterGrpc.EchoGreeterBlockingStub blockingStub = EchoGreeterGrpc.newBlockingStub(channel);
        EchoGreeterProto.EchoRequest echoRequest = EchoGreeterProto.EchoRequest.newBuilder()
                .setSize(10)
                .build();
        EchoGreeterProto.EchoReply echoReply = blockingStub.sayHello(echoRequest);
        log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
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
}
