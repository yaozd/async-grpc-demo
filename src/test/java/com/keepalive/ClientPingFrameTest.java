package com.keepalive;

import cn.hutool.core.thread.ThreadUtil;
import com.echo.grpc.EchoGreeterGrpc;
import com.echo.grpc.EchoGreeterProto;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 目的：
 * 哪些配置会触发PING操作
 *
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class ClientPingFrameTest extends AbstractKeepaliveTest {
    @After
    @SneakyThrows
    public void end() {
        log.error("[END]Call end().");
        Thread.currentThread().join();
    }

    /**
     * 前提：
     * 后端服务启动
     * {@link com.keepalive.ServerKeepaliveTest#keepaliveWithDirectExecutorTest()}
     * 模拟场景：
     * 后端服务出现阻塞，无法及时响应ping请求，客户端报错信息：io.grpc.StatusRuntimeException: UNAVAILABLE: Keepalive failed. The connection is likely gone
     * ping的间隔时间：
     * ping请求与主方法的间隔时间为10秒(PS:10秒为默认时间)
     */
    @Test
    public void ping10SecondTest() {
        ManagedChannel channel = NettyChannelBuilder
                .forTarget(target)
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
                .keepAliveTime(5, TimeUnit.SECONDS) //此时使用默认时间10秒
                /**
                 * keepAliveTimeout设置决定触发pingTimeout方法（PS:UNAVAILABLE:Keepalive failed. The connection is likely gone）的速度
                 * 默认为：10秒,最小值为10毫秒
                 */
                //.keepAliveTimeout(1, TimeUnit.NANOSECONDS)
                .keepAliveTimeout(10, TimeUnit.NANOSECONDS)
                //.keepAliveTimeout(10, TimeUnit.SECONDS)
                .usePlaintext()
                .build();
        EchoGreeterGrpc.EchoGreeterBlockingStub blockingStub = EchoGreeterGrpc.newBlockingStub(channel).withDeadlineAfter(20, TimeUnit.SECONDS);
        EchoGreeterProto.EchoRequest echoRequest = EchoGreeterProto.EchoRequest.newBuilder()
                .setSleepMills(TimeUnit.SECONDS.toMillis(60))
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
     * 前提：
     * 后端服务启动
     * {@link com.keepalive.ServerKeepaliveTest#keepaliveWithDirectExecutorTest()}
     * 模拟场景：
     * 后端服务出现阻塞，无法及时响应ping请求，客户端报错信息：io.grpc.StatusRuntimeException: UNAVAILABLE: Keepalive failed. The connection is likely gone
     * ping的间隔时间：
     * ping请求与主方法的间隔时间为30秒（PS:用户自定义时间）
     */
    @Test
    public void ping30SecondTest() {
        ManagedChannel channel = NettyChannelBuilder
                .forTarget(target)
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
                .keepAliveTime(30, TimeUnit.SECONDS) //此时使用用户自定义时间30秒
                /**
                 * keepAliveTimeout设置决定触发pingTimeout方法（PS:UNAVAILABLE:Keepalive failed. The connection is likely gone）的速度
                 * 默认为：10秒,最小值为10毫秒
                 */
                //.keepAliveTimeout(1, TimeUnit.NANOSECONDS)
                .keepAliveTimeout(10, TimeUnit.NANOSECONDS)
                //.keepAliveTimeout(10, TimeUnit.SECONDS)
                .usePlaintext()
                .build();
        //EchoGreeterGrpc.EchoGreeterBlockingStub blockingStub = EchoGreeterGrpc.newBlockingStub(channel).withDeadlineAfter(20, TimeUnit.SECONDS);
        EchoGreeterGrpc.EchoGreeterBlockingStub blockingStub = EchoGreeterGrpc.newBlockingStub(channel);
        EchoGreeterProto.EchoRequest echoRequest = EchoGreeterProto.EchoRequest.newBuilder()
                .setSleepMills(TimeUnit.SECONDS.toMillis(60))
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
     * 前提：
     * 后端服务启动
     * {@link com.keepalive.ServerKeepaliveTest#keepaliveWithDirectExecutorTest()}
     * 模拟场景：
     * 后端服务出现阻塞，无法及时响应ping请求，客户端报错信息：io.grpc.StatusRuntimeException: DEADLINE_EXCEEDED: deadline exceeded after 19890781200ns
     * PS:deadline时间小于keepAliveTime,此时会先触发deadline操作。
     * 触发deadline操作后，客户端后向服务端发送RST_STREAM帧，此时后端服务并不会中止当前正在处理的任务，只是在处理完成后不在发送给客户端而已
     * <p>
     * ping的间隔时间：
     * ping请求与主方法的间隔时间为30秒（PS:用户自定义时间）
     */
    @Test
    public void ping30SecondAndWithDeadlineAfterTest() {
        ManagedChannel channel = NettyChannelBuilder
                .forTarget(target)
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
                .keepAliveTime(30, TimeUnit.SECONDS) //此时使用用户自定义时间30秒
                /**
                 * keepAliveTimeout设置决定触发pingTimeout方法（PS:UNAVAILABLE:Keepalive failed. The connection is likely gone）的速度
                 * 默认为：10秒,最小值为10毫秒
                 */
                //.keepAliveTimeout(1, TimeUnit.NANOSECONDS)
                .keepAliveTimeout(10, TimeUnit.NANOSECONDS)
                //.keepAliveTimeout(10, TimeUnit.SECONDS)
                .usePlaintext()
                .build();
        //
        EchoGreeterGrpc.EchoGreeterBlockingStub blockingStub = EchoGreeterGrpc.newBlockingStub(channel).withDeadlineAfter(20, TimeUnit.SECONDS);
        EchoGreeterProto.EchoRequest echoRequest = EchoGreeterProto.EchoRequest.newBuilder()
                .setSleepMills(TimeUnit.SECONDS.toMillis(60))
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
     * 前提：
     * 后端服务启动
     * {@link com.keepalive.ServerKeepaliveTest#keepaliveWithDirectExecutorTest()}
     * futureStub.sayHello(echoRequest).get(5, TimeUnit.SECONDS)
     * 客户端报错信息：java.util.concurrent.TimeoutException: Waited 5 seconds for io.grpc.stub.ClientCalls$GrpcFuture
     */
    @Test
    public void ping30SecondAndWithDeadlineAfterByFutureChannelTest() {
        ManagedChannel channel = NettyChannelBuilder
                .forTarget(target)
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
                .keepAliveTime(30, TimeUnit.SECONDS) //此时使用用户自定义时间30秒
                /**
                 * keepAliveTimeout设置决定触发pingTimeout方法（PS:UNAVAILABLE:Keepalive failed. The connection is likely gone）的速度
                 * 默认为：10秒,最小值为10毫秒
                 */
                //.keepAliveTimeout(1, TimeUnit.NANOSECONDS)
                .keepAliveTimeout(10, TimeUnit.NANOSECONDS)
                //.keepAliveTimeout(10, TimeUnit.SECONDS)
                .usePlaintext()
                .build();
        //
        EchoGreeterGrpc.EchoGreeterFutureStub futureStub = EchoGreeterGrpc.newFutureStub(channel).withDeadlineAfter(20, TimeUnit.SECONDS);
        EchoGreeterProto.EchoRequest echoRequest = EchoGreeterProto.EchoRequest.newBuilder()
                .setSleepMills(TimeUnit.SECONDS.toMillis(60))
                .setSize(10)
                .build();
        try {
            //监听模式
            //ListenableFuture<EchoGreeterProto.EchoReply> echoReplyListenableFuture = futureStub.sayHello(echoRequest);
            //阻塞模式与newBlockingStub相同，不同之处在于：可以自定义每一个方法的timeout时间而已
            //EchoGreeterProto.EchoReply echoReply = futureStub.sayHello(echoRequest).get(); //默认使用futureStub统一时间 ：20秒
            EchoGreeterProto.EchoReply echoReply = futureStub.sayHello(echoRequest).get(5, TimeUnit.SECONDS);//使用用户自定时间： 5秒
            log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
        } catch (Exception ex) {
            log.error("", ex);
        }
    }

    /**
     * 前提：
     * 后端服务启动
     * {@link com.keepalive.ServerKeepaliveTest#keepaliveWithDirectExecutorTest()}
     * 场景：
     * keepAliveTime(10, TimeUnit.SECONDS)<futureStub.sayHello(echoRequest).get(15, TimeUnit.SECONDS)<EchoGreeterGrpc.newFutureStub(channel).withDeadlineAfter(20, TimeUnit.SECONDS)
     * 10s<15s<20s
     * 在后端服务发生阻塞，无法响应客户端ping请求时，会触发客户端：io.grpc.StatusRuntimeException: UNAVAILABLE: Keepalive failed. The connection is likely gone异常。
     * 但实际原因为：后端服务发生阻塞，无法响应客户端ping请求时。因此如果触发客户端deadline或TimeoutException异常更为准确。
     * 所以本测试的示例配置是不恰当的。
     */
    @Test
    public void ping10SecondAndWithDeadlineAfterByFutureChannelTest() {
        ManagedChannel channel = NettyChannelBuilder
                .forTarget(target)
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
                .keepAliveTime(10, TimeUnit.SECONDS) //此时使用用户自定义时间10秒
                /**
                 * keepAliveTimeout设置决定触发pingTimeout方法（PS:UNAVAILABLE:Keepalive failed. The connection is likely gone）的速度
                 * 默认为：10秒,最小值为10毫秒
                 */
                //.keepAliveTimeout(1, TimeUnit.NANOSECONDS)
                .keepAliveTimeout(10, TimeUnit.NANOSECONDS)
                //.keepAliveTimeout(10, TimeUnit.SECONDS)
                .usePlaintext()
                .build();
        //
        EchoGreeterGrpc.EchoGreeterFutureStub futureStub = EchoGreeterGrpc.newFutureStub(channel).withDeadlineAfter(20, TimeUnit.SECONDS);
        EchoGreeterProto.EchoRequest echoRequest = EchoGreeterProto.EchoRequest.newBuilder()
                .setSleepMills(TimeUnit.SECONDS.toMillis(60))
                .setSize(10)
                .build();
        try {
            //监听模式
            //ListenableFuture<EchoGreeterProto.EchoReply> echoReplyListenableFuture = futureStub.sayHello(echoRequest);
            //阻塞模式与newBlockingStub相同，不同之处在于：可以自定义每一个方法的timeout时间而已
            //EchoGreeterProto.EchoReply echoReply = futureStub.sayHello(echoRequest).get(); //默认使用futureStub统一时间 ：20秒
            EchoGreeterProto.EchoReply echoReply = futureStub.sayHello(echoRequest).get(15, TimeUnit.SECONDS);//使用用户自定时间： 5秒
            log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
        } catch (Exception ex) {
            log.error("", ex);
        }
    }

    /**
     * 前提：
     * 后端服务启动
     * {@link com.keepalive.ServerKeepaliveTest#keepalivePermitKeepAliveTimeWithExecutorTest()}
     * 场景：
     * keepAliveTime(10, TimeUnit.SECONDS)<futureStub.sayHello(echoRequest).get(40, TimeUnit.SECONDS)<newFutureStub(channel).withDeadlineAfter(60, TimeUnit.SECONDS);
     * 10*3=30<40<60
     * 此时客户端会触发3此PING请求
     * 客户端：10*3=30<后端服务：.permitKeepAliveTime(5, TimeUnit.MINUTES) //permitKeepAliveTime默认值为5分钟
     * 超过了后端规定的速率（permitKeepAliveTime(5, TimeUnit.MINUTES)代表客户端5分钟内最多发2次ping请求）
     * <p>
     * 前提是后端服务可以正常响应ping请求，超过后端规定的速率后，客户端报错：io.grpc.StatusRuntimeException: RESOURCE_EXHAUSTED: Bandwidth exhausted异常
     */
    @Test
    public void ping10SecondAndWithDeadlineAfterByFutureChannelAndServerWithKeepalivePermitKeepAliveTimeTest() {
        ManagedChannel channel = NettyChannelBuilder
                .forTarget(target)
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
                .keepAliveTime(10, TimeUnit.SECONDS) //此时使用用户自定义时间10秒
                /**
                 * keepAliveTimeout设置决定触发pingTimeout方法（PS:UNAVAILABLE:Keepalive failed. The connection is likely gone）的速度
                 * 默认为：10秒,最小值为10毫秒
                 */
                //.keepAliveTimeout(1, TimeUnit.NANOSECONDS)
                .keepAliveTimeout(10, TimeUnit.NANOSECONDS)
                //.keepAliveTimeout(10, TimeUnit.SECONDS)
                .usePlaintext()
                .build();
        //
        EchoGreeterGrpc.EchoGreeterFutureStub futureStub = EchoGreeterGrpc.newFutureStub(channel).withDeadlineAfter(60, TimeUnit.SECONDS);
        EchoGreeterProto.EchoRequest echoRequest = EchoGreeterProto.EchoRequest.newBuilder()
                .setSleepMills(TimeUnit.SECONDS.toMillis(60))
                .setSize(10)
                .build();
        try {
            //监听模式
            //ListenableFuture<EchoGreeterProto.EchoReply> echoReplyListenableFuture = futureStub.sayHello(echoRequest);
            //阻塞模式与newBlockingStub相同，不同之处在于：可以自定义每一个方法的timeout时间而已
            //EchoGreeterProto.EchoReply echoReply = futureStub.sayHello(echoRequest).get(); //默认使用futureStub统一时间 ：20秒
            EchoGreeterProto.EchoReply echoReply = futureStub.sayHello(echoRequest).get(40, TimeUnit.SECONDS);//使用用户自定时间： 40秒
            log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
        } catch (Exception ex) {
            log.error("", ex);
        }
    }

    /**
     * 场景：
     * 不设置keepAliveTime（//.keepAliveTime(10, TimeUnit.SECONDS) ）
     * <p>
     * 如果不设置：keepAliveTime，则默认为禁用客户端保持连接
     * 相当于客户端不会发送PIING请求
     * 客户端会触发：
     * futureStub.sayHello(echoRequest).get(40, TimeUnit.SECONDS)
     * 或
     * EchoGreeterGrpc.newFutureStub(channel).withDeadlineAfter(60, TimeUnit.SECONDS);
     * <p>
     * 本示例：futureStub.sayHello(echoRequest).get(40, TimeUnit.SECONDS)<EchoGreeterGrpc.newFutureStub(channel).withDeadlineAfter(60, TimeUnit.SECONDS);
     * 会触发客户端：java.util.concurrent.TimeoutException: Waited 40 seconds for io.grpc.stub.ClientCalls异常！
     */
    @Test
    public void noKeepAliveTimeButWithDeadlineAfterByFutureChannelAndServerWithKeepalivePermitKeepAliveTimeTest() {
        ManagedChannel channel = NettyChannelBuilder
                .forTarget(target)
                /**
                 * 代表：空闲模式也要触发ping请求
                 * 设置当连接上没有未完成的RPC时是否执行keepalive。
                 * *默认为{@code false}。
                 */
                .keepAliveWithoutCalls(true)
                /**
                 * 如果不设置：keepAliveTime，则默认为禁用客户端保持连接
                 * 相当于客户端不会发送PIING请求
                 */
                /**
                 * 空闲模式与非空闲模式（有请求正在处理）：触发
                 * 如果长时间没有收到读时，则发送ping来判断当前连接是否存活。
                 * 默认为20秒，最小为10秒
                 * 如果keepAliveTimeout(1, TimeUnit.SECONDS)则设置为最小值10秒，
                 * 如果客户端频发发送ping请求，则出发too_many_pings
                 */
                //.keepAliveTime(10, TimeUnit.SECONDS) //此时使用用户自定义时间10秒
                /**
                 * keepAliveTimeout设置决定触发pingTimeout方法（PS:UNAVAILABLE:Keepalive failed. The connection is likely gone）的速度
                 * 默认为：10秒,最小值为10毫秒
                 */
                //.keepAliveTimeout(1, TimeUnit.NANOSECONDS)
                .keepAliveTimeout(10, TimeUnit.NANOSECONDS)
                //.keepAliveTimeout(10, TimeUnit.SECONDS)
                .usePlaintext()
                .build();
        //
        EchoGreeterGrpc.EchoGreeterFutureStub futureStub = EchoGreeterGrpc.newFutureStub(channel).withDeadlineAfter(60, TimeUnit.SECONDS);
        EchoGreeterProto.EchoRequest echoRequest = EchoGreeterProto.EchoRequest.newBuilder()
                .setSleepMills(TimeUnit.SECONDS.toMillis(60))
                .setSize(10)
                .build();
        try {
            //监听模式
            //ListenableFuture<EchoGreeterProto.EchoReply> echoReplyListenableFuture = futureStub.sayHello(echoRequest);
            //阻塞模式与newBlockingStub相同，不同之处在于：可以自定义每一个方法的timeout时间而已
            //EchoGreeterProto.EchoReply echoReply = futureStub.sayHello(echoRequest).get(); //默认使用futureStub统一时间 ：20秒
            EchoGreeterProto.EchoReply echoReply = futureStub.sayHello(echoRequest).get(40, TimeUnit.SECONDS);//使用用户自定时间： 40秒
            log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
        } catch (Exception ex) {
            log.error("", ex);
        }
    }

    /**
     * 前提：
     * 后端服务启动
     * //场景：统计客户端：ping的调用次数。
     * {@link com.keepalive.ServerKeepaliveTest#keepaliveWithExecutorTest()}
     * //场景：模拟触发：too_many_pings
     * {@link com.keepalive.ServerKeepaliveTest#keepalivePermitKeepAliveTimeWithExecutorTest()}
     * 并发模式下，观察ping的请求次数
     */
    @Test
    public void keepaliveConcurrentModeTest() {
        ManagedChannel channel = NettyChannelBuilder
                .forTarget(target)
                /**
                 * 代表：空闲模式也要触发ping请求
                 * 设置当连接上没有未完成的RPC时是否执行keepalive。
                 * *默认为{@code false}。
                 */
                .keepAliveWithoutCalls(true)
                /**
                 * 如果不设置：keepAliveTime，则默认为禁用客户端保持连接
                 * 相当于客户端不会发送PIING请求
                 */
                /**
                 * 空闲模式与非空闲模式（有请求正在处理）：触发
                 * 如果长时间没有收到读时，则发送ping来判断当前连接是否存活。
                 * 默认为20秒，最小为10秒
                 * 如果keepAliveTimeout(1, TimeUnit.SECONDS)则设置为最小值10秒，
                 * 如果客户端频发发送ping请求，则出发too_many_pings
                 */
                .keepAliveTime(10, TimeUnit.SECONDS) //此时使用用户自定义时间10秒
                /**
                 * keepAliveTimeout设置决定触发pingTimeout方法（PS:UNAVAILABLE:Keepalive failed. The connection is likely gone）的速度
                 * 默认为：10秒,最小值为10毫秒
                 */
                //.keepAliveTimeout(1, TimeUnit.NANOSECONDS)
                .keepAliveTimeout(10, TimeUnit.NANOSECONDS)
                //.keepAliveTimeout(10, TimeUnit.SECONDS)
                .usePlaintext()
                .build();
        //
        EchoGreeterGrpc.EchoGreeterFutureStub futureStub = EchoGreeterGrpc.newFutureStub(channel).withDeadlineAfter(60, TimeUnit.SECONDS);
        EchoGreeterProto.EchoRequest echoRequest = EchoGreeterProto.EchoRequest.newBuilder()
                .setSleepMills(TimeUnit.SECONDS.toMillis(60))
                .setSize(10)
                .build();
        int nThreads = 50;
        ExecutorService executorService = ThreadUtil.newExecutor(nThreads);
        for (int i = 0; i < nThreads; i++) {
            executorService.execute(() -> {
                try {
                    //监听模式
                    //ListenableFuture<EchoGreeterProto.EchoReply> echoReplyListenableFuture = futureStub.sayHello(echoRequest);
                    //阻塞模式与newBlockingStub相同，不同之处在于：可以自定义每一个方法的timeout时间而已
                    //EchoGreeterProto.EchoReply echoReply = futureStub.sayHello(echoRequest).get(); //默认使用futureStub统一时间 ：20秒
                    EchoGreeterProto.EchoReply echoReply = futureStub.sayHello(echoRequest).get(40, TimeUnit.SECONDS);//使用用户自定时间： 40秒
                    log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
                } catch (Exception ex) {
                    log.error("", ex);
                }
            });
        }

    }

    /**
     * 使用同一个stub
     * 多个方法调用使用同一个stub的时候
     * 在方法调用时，加入sleep，模拟业务处理时长
     * 前提：
     * 后端服务启动
     * //场景：统计客户端：ping的调用次数。
     * {@link com.keepalive.ServerKeepaliveTest#keepaliveWithExecutorTest()}
     * //场景：模拟触发：too_many_pings
     * {@link com.keepalive.ServerKeepaliveTest#keepalivePermitKeepAliveTimeWithExecutorTest()}
     * 并发模式下，观察ping的请求次数
     * 客户端调用场景说明：
     * 多个方法调用使用同一个stub的时候，deadline的计时是同一个，因此会出现部分方法成功，部分方法执行失败。
     * PS:执行失败提示信息：
     * 开始异常：
     * StatusRuntimeException: RESOURCE_EXHAUSTED: Bandwidth exhausted
     * 后面异常：
     * StatusRuntimeException: DEADLINE_EXCEEDED: deadline exceeded: -185.018221801s from now
     * StatusRuntimeException: DEADLINE_EXCEEDED: deadline exceeded: -185.018221801s from now
     * 问题说明：
     * 因为多个方法调用使用同一个stub的时候，触发deadline后，这个stub下的所有
     */
    @Test
    public void keepaliveConcurrentModeWithSameStubAndSleepInCallMethodTest() {
        ManagedChannel channel = NettyChannelBuilder
                .forTarget(target)
                /**
                 * 代表：空闲模式也要触发ping请求
                 * 设置当连接上没有未完成的RPC时是否执行keepalive。
                 * *默认为{@code false}。
                 */
                .keepAliveWithoutCalls(true)
                /**
                 * 如果不设置：keepAliveTime，则默认为禁用客户端保持连接
                 * 相当于客户端不会发送PIING请求
                 */
                /**
                 * 空闲模式与非空闲模式（有请求正在处理）：触发
                 * 如果长时间没有收到读时，则发送ping来判断当前连接是否存活。
                 * 默认为20秒，最小为10秒
                 * 如果keepAliveTimeout(1, TimeUnit.SECONDS)则设置为最小值10秒，
                 * 如果客户端频发发送ping请求，则出发too_many_pings
                 */
                .keepAliveTime(10, TimeUnit.SECONDS) //此时使用用户自定义时间10秒
                /**
                 * keepAliveTimeout设置决定触发pingTimeout方法（PS:UNAVAILABLE:Keepalive failed. The connection is likely gone）的速度
                 * 默认为：10秒,最小值为10毫秒
                 */
                //.keepAliveTimeout(1, TimeUnit.NANOSECONDS)
                .keepAliveTimeout(10, TimeUnit.NANOSECONDS)
                //.keepAliveTimeout(10, TimeUnit.SECONDS)
                .usePlaintext()
                .build();
        //
        EchoGreeterGrpc.EchoGreeterFutureStub futureStub = EchoGreeterGrpc.newFutureStub(channel).withDeadlineAfter(60, TimeUnit.SECONDS);
        EchoGreeterProto.EchoRequest echoRequest = EchoGreeterProto.EchoRequest.newBuilder()
                .setSleepMills(TimeUnit.SECONDS.toMillis(60))
                .setSize(10)
                .build();
        int nThreads = 50;
        ExecutorService executorService = ThreadUtil.newExecutor(nThreads);
        AtomicInteger nSleep = new AtomicInteger();
        for (int i = 0; i < nThreads; i++) {
            executorService.execute(() -> {
                try {
                    ThreadUtil.sleep(5 * nSleep.getAndIncrement(), TimeUnit.SECONDS);
                    //监听模式
                    //ListenableFuture<EchoGreeterProto.EchoReply> echoReplyListenableFuture = futureStub.sayHello(echoRequest);
                    //阻塞模式与newBlockingStub相同，不同之处在于：可以自定义每一个方法的timeout时间而已
                    //EchoGreeterProto.EchoReply echoReply = futureStub.sayHello(echoRequest).get(); //默认使用futureStub统一时间 ：20秒
                    EchoGreeterProto.EchoReply echoReply = futureStub.sayHello(echoRequest).get(40, TimeUnit.SECONDS);//使用用户自定时间： 40秒
                    log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
                } catch (Exception ex) {
                    log.error("", ex);
                }
            });
        }
    }

    /**
     * 使用同一个stub
     * 多个方法调用使用同一个stub的时候
     * 在方法调用时，加入sleep，模拟业务处理时长
     * 前提：
     * 后端服务启动
     * //场景：允许服务器接受任意数量的ping
     * {@link com.keepalive.ServerKeepaliveTest#keepaliveNoTooManyPingsWithExecutorTest()}
     * //场景：简单说：是否允许客户端在没有请求时，发送ping请求来保持连接,设置permitKeepAliveWithoutCalls(false)
     * {@link com.keepalive.ServerKeepaliveTest#keepaliveNoPermitKeepAliveTimeWithExecutorTest()}
     * //场景：统计客户端：ping的调用次数。
     * {@link com.keepalive.ServerKeepaliveTest#keepaliveWithExecutorTest()}
     * //场景：模拟触发：too_many_pings
     * {@link com.keepalive.ServerKeepaliveTest#keepalivePermitKeepAliveTimeWithExecutorTest()}
     * 并发模式下，观察ping的请求次数
     * 客户端调用场景说明：
     * 多个方法调用使用同一个stub的时候，deadline的计时是同一个，因此会出现部分方法成功，部分方法执行失败。
     * PS:执行失败提示信息：
     * 开始异常：
     * StatusRuntimeException: RESOURCE_EXHAUSTED: Bandwidth exhausted
     * 后面异常：
     * StatusRuntimeException: DEADLINE_EXCEEDED: deadline exceeded: -185.018221801s from now
     * StatusRuntimeException: DEADLINE_EXCEEDED: deadline exceeded: -185.018221801s from now
     * 问题说明：
     * 因为多个方法调用使用同一个stub的时候，触发deadline后，这个stub下的所有
     * =======================================================================================
     * keepalive的变化
     * io.grpc.netty.NettyClientHandler$3 onGoAwayReceived
     * 警告: Received GOAWAY with ENHANCE_YOUR_CALM. Debug data: {1}
     * 二月 22, 2021 11:05:40 上午 io.grpc.internal.AtomicBackoff$State backoff
     * 警告: Increased keepalive time nanos to 40,000,000,000
     * PS:
     * AtomicBackoff增加keepalive time ：10秒 变为 40秒
     */
    @Test
    public void keepaliveConcurrentModeWithSameStubAndDifferentDeadlineAndSleepInCallMethodTest() {
        ManagedChannel channel = NettyChannelBuilder
                .forTarget(target)
                /**
                 * 代表：空闲模式也要触发ping请求
                 * 设置当连接上没有未完成的RPC时是否执行keepalive。
                 * *默认为{@code false}。
                 */
                .keepAliveWithoutCalls(true)
                /**
                 * 如果不设置：keepAliveTime，则默认为禁用客户端保持连接
                 * 相当于客户端不会发送PIING请求
                 */
                /**
                 * 空闲模式与非空闲模式（有请求正在处理）：触发
                 * 如果长时间没有收到读时，则发送ping来判断当前连接是否存活。
                 * 默认为20秒，最小为10秒
                 * 如果keepAliveTimeout(1, TimeUnit.SECONDS)则设置为最小值10秒，
                 * 如果客户端频发发送ping请求，则出发too_many_pings
                 */
                .keepAliveTime(10, TimeUnit.SECONDS) //此时使用用户自定义时间10秒
                /**
                 * keepAliveTimeout设置决定触发pingTimeout方法（PS:UNAVAILABLE:Keepalive failed. The connection is likely gone）的速度
                 * 默认为：10秒,最小值为10毫秒
                 */
                //.keepAliveTimeout(1, TimeUnit.NANOSECONDS)
                .keepAliveTimeout(10, TimeUnit.NANOSECONDS)
                //.keepAliveTimeout(10, TimeUnit.SECONDS)
                .usePlaintext()
                .build();
        //
        EchoGreeterGrpc.EchoGreeterFutureStub parentStub = EchoGreeterGrpc.newFutureStub(channel).withDeadlineAfter(5, TimeUnit.SECONDS);
        EchoGreeterProto.EchoRequest echoRequest = EchoGreeterProto.EchoRequest.newBuilder()
                .setSleepMills(TimeUnit.SECONDS.toMillis(30))
                .setSize(10)
                .build();
        //int nThreads = 50;
        int nThreads = 200;
        ExecutorService executorService = ThreadUtil.newExecutor(nThreads);
        AtomicInteger nSleep = new AtomicInteger();
        for (int i = 0; i < nThreads; i++) {
            executorService.execute(() -> {
                try {
                    int num = nSleep.getAndIncrement();
                    log.error("NSleep:[{}]", num);
                    ThreadUtil.sleep(1 * num, TimeUnit.SECONDS);
                    //使用不同的deadline,创建不同的stub对象
                    EchoGreeterGrpc.EchoGreeterFutureStub futureStub = parentStub.withDeadlineAfter(60, TimeUnit.SECONDS);
                    log.info(futureStub.toString());
                    //监听模式
                    //ListenableFuture<EchoGreeterProto.EchoReply> echoReplyListenableFuture = futureStub.sayHello(echoRequest);
                    //阻塞模式与newBlockingStub相同，不同之处在于：可以自定义每一个方法的timeout时间而已
                    //EchoGreeterProto.EchoReply echoReply = futureStub.sayHello(echoRequest).get(); //默认使用futureStub统一时间 ：20秒
                    EchoGreeterProto.EchoReply echoReply = futureStub.sayHello(echoRequest).get(40, TimeUnit.SECONDS);//使用用户自定时间： 40秒
                    log.error("NSleep:[{}],At time:[{}],message:[{}].", num, echoReply.getAtTime(), echoReply.getMessage());
                } catch (Exception ex) {
                    log.error("", ex);
                }
            });
        }
    }

    /**
     * 使用不同的stub
     * 每个方法都使用一个独立的stub的情况
     * 在方法调用时，加入sleep，模拟业务处理时长
     * 前提：
     * 后端服务启动
     * //场景：统计客户端：ping的调用次数。
     * {@link com.keepalive.ServerKeepaliveTest#keepaliveWithExecutorTest()}
     * //场景：模拟触发：too_many_pings
     * {@link com.keepalive.ServerKeepaliveTest#keepalivePermitKeepAliveTimeWithExecutorTest()}
     * 并发模式下，观察ping的请求次数
     * 客户端调用场景说明：
     * 每个方法都使用一个独立的stub的情况，因此会出现部分方法成功，部分方法执行失败。
     * PS:执行失败提示信息：
     * 开始异常：
     * StatusRuntimeException: RESOURCE_EXHAUSTED: Bandwidth exhausted
     * 后面异常：
     * TimeoutException: Waited 40 seconds for io.grpc.stub.ClientCalls
     * TimeoutException: Waited 40 seconds for io.grpc.stub.ClientCalls
     * TimeoutException: Waited 40 seconds for io.grpc.stub.ClientCalls
     * PS:
     * futureStub.sayHello(echoRequest).get(40, TimeUnit.SECONDS)<newFutureStub(channel).withDeadlineAfter(60, TimeUnit.SECONDS)
     * 优先触发（方法级别的超时：TimeoutException）而非DEADLINE_EXCEEDED: deadline exceeded
     */
    @Test
    public void keepaliveConcurrentModeWithDifferentStubAndSleepInCallMethod2Test() {
        ManagedChannel channel = NettyChannelBuilder
                .forTarget(target)
                /**
                 * 代表：空闲模式也要触发ping请求
                 * 设置当连接上没有未完成的RPC时是否执行keepalive。
                 * *默认为{@code false}。
                 */
                .keepAliveWithoutCalls(true)
                /**
                 * 如果不设置：keepAliveTime，则默认为禁用客户端保持连接
                 * 相当于客户端不会发送PIING请求
                 */
                /**
                 * 空闲模式与非空闲模式（有请求正在处理）：触发
                 * 如果长时间没有收到读时，则发送ping来判断当前连接是否存活。
                 * 默认为20秒，最小为10秒
                 * 如果keepAliveTimeout(1, TimeUnit.SECONDS)则设置为最小值10秒，
                 * 如果客户端频发发送ping请求，则出发too_many_pings
                 */
                .keepAliveTime(10, TimeUnit.SECONDS) //此时使用用户自定义时间10秒
                /**
                 * keepAliveTimeout设置决定触发pingTimeout方法（PS:UNAVAILABLE:Keepalive failed. The connection is likely gone）的速度
                 * 默认为：10秒,最小值为10毫秒
                 */
                //.keepAliveTimeout(1, TimeUnit.NANOSECONDS)
                .keepAliveTimeout(10, TimeUnit.NANOSECONDS)
                //.keepAliveTimeout(10, TimeUnit.SECONDS)
                .usePlaintext()
                .build();
        //
        //EchoGreeterGrpc.EchoGreeterFutureStub futureStub = EchoGreeterGrpc.newFutureStub(channel).withDeadlineAfter(60, TimeUnit.SECONDS);
        EchoGreeterProto.EchoRequest echoRequest = EchoGreeterProto.EchoRequest.newBuilder()
                .setSleepMills(TimeUnit.SECONDS.toMillis(60))
                .setSize(10)
                .build();
        int nThreads = 50;
        ExecutorService executorService = ThreadUtil.newExecutor(nThreads);
        AtomicInteger nSleep = new AtomicInteger();
        for (int i = 0; i < nThreads; i++) {
            executorService.execute(() -> {
                try {
                    ThreadUtil.sleep(5 * nSleep.getAndIncrement(), TimeUnit.SECONDS);
                    EchoGreeterGrpc.EchoGreeterFutureStub futureStub = EchoGreeterGrpc.newFutureStub(channel).withDeadlineAfter(60, TimeUnit.SECONDS);
                    //监听模式
                    //ListenableFuture<EchoGreeterProto.EchoReply> echoReplyListenableFuture = futureStub.sayHello(echoRequest);
                    //阻塞模式与newBlockingStub相同，不同之处在于：可以自定义每一个方法的timeout时间而已
                    //EchoGreeterProto.EchoReply echoReply = futureStub.sayHello(echoRequest).get(); //默认使用futureStub统一时间 ：20秒
                    EchoGreeterProto.EchoReply echoReply = futureStub.sayHello(echoRequest).get(40, TimeUnit.SECONDS);//使用用户自定时间： 40秒
                    log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
                } catch (Exception ex) {
                    log.error("", ex);
                }
            });
        }
    }

}
