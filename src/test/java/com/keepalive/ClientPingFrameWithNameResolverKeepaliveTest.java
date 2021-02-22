package com.keepalive;

import cn.hutool.core.thread.ThreadUtil;
import com.echo.grpc.EchoGreeterGrpc;
import com.echo.grpc.EchoGreeterProto;
import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.ManagedChannel;
import io.grpc.NameResolver;
import io.grpc.netty.NettyChannelBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Test;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class ClientPingFrameWithNameResolverKeepaliveTest extends AbstractKeepaliveTest {
    @After
    @SneakyThrows
    public void end() {
        log.error("[END]Call end().");
        Thread.currentThread().join();
    }

    /**
     * 判断channel负载均衡的情况
     * 流量不均衡。如果把所有流量都打到同一个后端服务节点，也会导致此节点服务不可用。
     * 使用同一个stub
     * 多个方法调用使用同一个stub的时候
     * 在方法调用时，加入sleep，模拟业务处理时长
     * 前提：
     * 后端服务启动
     * //场景：启动一组后端服务,允许服务器接受任意数量的ping
     * {@link com.keepalive.ServerKeepaliveTest#serverGroupKeepaliveNoTooManyPingsWithExecutorTest()}
     * 并发模式下，观察ping的请求次数
     * 客户端调用场景说明：
     * 客户端的负载均衡模式：
     * 默认使用PickFirstLoadBalancerProvider模式，会导致流量不均衡。如果把所有流量都打到同一个后端服务节点，也会导致此节点服务不可用。
     * 推荐使用：defaultLoadBalancingPolicy("round_robin")轮训模式可以更好平衡请求。
     */
    @Test
    public void keepaliveConcurrentModeWithSameStubAndDifferentDeadlineAndSleepInCallMethodTest() {
        ManagedChannel channel = newChannel();
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

    private ManagedChannel newChannel() {
        return NettyChannelBuilder
                .forTarget(target)
                .nameResolverFactory(new NameResolver.Factory() {
                    private final String scheme = "yzd";

                    @Nullable
                    @Override
                    public NameResolver newNameResolver(URI uri, Attributes attributes) {
                        //模拟测试时，可以此判断注释掉
                        //if(!scheme.equals(uri.getScheme())){
                        //      return null;
                        // }
                        return new NameResolver() {

                            private Listener listener;
                            private ScheduledExecutorService executorService;

                            @Override
                            public String getServiceAuthority() {
                                return uri.getAuthority() == null ? String.valueOf(uri) : uri.getAuthority();
                            }

                            /**
                             * 更新服务器地址：listener.onAddresses
                             * @param listener
                             */
                            @Override
                            public void start(Listener listener) {
                                log.info("Start !");
                                this.listener = listener;
                                this.executorService = new ScheduledThreadPoolExecutor(1);
                                this.executorService.scheduleWithFixedDelay(() -> {

                                    List<EquivalentAddressGroup> servers = new ArrayList<>();
                                    for (int i = 0; i < serverCount; i++) {
                                        int newPort = port + i;
                                        servers.add(new EquivalentAddressGroup((new InetSocketAddress("localhost", newPort))));
                                    }
                                    //Handles updates on resolved addresses and attributes.
                                    this.listener.onAddresses(servers, Attributes.EMPTY);
                                    log.info("Refresh !");
                                    //需要更新的时候会调用refresh方法
                                    refresh();
                                }, 0, 5, TimeUnit.SECONDS);
                            }

                            @Override
                            public void shutdown() {

                            }
                        };
                    }

                    @Override
                    public String getDefaultScheme() {
                        return scheme;
                    }
                })
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
                //服务发现与负载均衡的默认值
                //DnsNameResolverProvider and PickFirstLoadBalancerProvider are the defaults
                //.loadBalancerFactory(RoundRobinLoadBalancerFactory.getInstance())
                /**
                 * 设置负载均衡为轮训模式
                 * 轮训模式可以更好平衡请求。
                 * https://github.com/grpc/grpc-java/issues/1771
                 */
                .defaultLoadBalancingPolicy("round_robin")
                /**
                 *  选择第一个可以使用的节点
                 *  PS:这种模式下的会发生流量负载不均匀问题，暂时不推荐
                 * {@link io.grpc.internal.PickFirstLoadBalancerProvider}
                 */
                //.defaultLoadBalancingPolicy("pick_first")
                .usePlaintext()
                .build();
    }

}
