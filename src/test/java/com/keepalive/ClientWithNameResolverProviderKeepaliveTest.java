package com.keepalive;

import com.echo.grpc.EchoGreeterGrpc;
import com.echo.grpc.EchoGreeterProto;
import io.grpc.*;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.channel.ChannelOption;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class ClientWithNameResolverProviderKeepaliveTest extends AbstractKeepaliveTest {
    private ManagedChannel channel;

    @Before
    public void init() {
        newManagedChannel();
//        newNettyChannel();
    }

    private void newNettyChannel() {
        channel = NettyChannelBuilder
                .forTarget(target)
                /**
                * grpc客户端连接超时时长，如果没有配置，默认使用Netty的ChannelOption.CONNECT_TIMEOUT_MILLIS
                 * ChannelOption.CONNECT_TIMEOUT_MILLIS： (一般用于Bootstrap或者childOption)
                 * Netty参数，连接超时毫秒数，默认值30000毫秒即30秒。
                 * netty系列之ChannelOption
                 * https://blog.csdn.net/zhongzunfa/article/details/94590670
                 */
                .withOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10)
                .usePlaintext()
                .build();
    }

    private void newManagedChannel() {
        channel = ManagedChannelBuilder
                .forTarget("localhost:8899")
                //.nameResolverFactory(new DnsNameResolverProvider())
                .nameResolverFactory(new NameResolver.Factory() {
                    private final String scheme ="yzd";
                    @Nullable
                    @Override
                    public NameResolver newNameResolver(URI uri, Attributes attributes) {
                        //模拟测试时，可以此判断注释掉
                        if(!scheme.equals(uri.getScheme())){
                            return null;
                        }
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
                                    log.info("Refresh !");
                                    //需要更新的时候会调用refresh方法
                                    refresh();
                                    List<EquivalentAddressGroup> servers = new ArrayList<>();
                                    servers.add(new EquivalentAddressGroup((new InetSocketAddress("localhost", 50052))));
                                    servers.add(new EquivalentAddressGroup((new InetSocketAddress("localhost", 50051))));
                                    //Handles updates on resolved addresses and attributes.
                                    this.listener.onAddresses(servers, Attributes.EMPTY);
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
                 * 增加重试
                 */
                .enableRetry()
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
    *  通过数学公式的方法来讲解问题：y=(x+z+c)+t;
    *
    *  场景：客户端与后端服务之间的网络不通过。连接超时时间过长造成。
    *  方法检测的PING操作产生的UNAVAILABLE:Keepalive failed. The connection is likely gone异常，在UNAVAILABLE:io exception|UNAVAILABLE:connection timeout之间触发。
    *  所以提示错误为UNAVAILABLE:Keepalive failed. The connection is likely gone。
    *  =======================================================================
    *  影响连接超时时间时长的因素有哪些？
    *  1.Netty的ChannelOption.CONNECT_TIMEOUT_MILLIS （PS:Netty参数，连接超时毫秒数，默认值30000毫秒即30秒。）
    *  2.linux系统参数：net.ipv4.tcp_syn_retries(PS:net.ipv4.tcp_syn_retries = 6，默认值6次)
    *  此时客户端发起syn_retries，TCP重试操作
    *  TCP重试的次数与linux系统参数：net.ipv4.tcp_syn_retries有关
    *  TCP 握手的 SYN 包超时重试按照 2 的幂来 backoff
    *  参考：
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
                //ThreadUtil.sleep(10, TimeUnit.SECONDS);
                EchoGreeterProto.EchoReply echoReply = greeterFutureStub.sayHello(request).get(5, TimeUnit.SECONDS);
                log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
            } catch (Exception ex) {
                log.error("", ex);
            }
        }
        //ThreadUtil.sleep(60, TimeUnit.SECONDS);
    }
}
