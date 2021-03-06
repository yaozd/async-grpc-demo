package com.keepalive;

import com.echo.EchoGreeterServiceImpl;
import com.opentracinglog.ServerTracingLogInterceptor;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class ServerKeepaliveTest extends AbstractKeepaliveTest {

    /**
     * 模拟触发PING操作场景，推荐使用directExecutor模式
     */
    @Test
    @SneakyThrows
    public void keepaliveWithDirectExecutorTest() {
        Server server = NettyServerBuilder.forPort(port)
                .intercept(new ServerTracingLogInterceptor())
                //只用一个线程：然后在当前线程产生阻塞，模拟服务处理缓慢
                .directExecutor()
                /**
                 * too_many_pings:Sent GOAWAY
                 * 在规定的时间内发送了超过2次ping请求
                 */
                .permitKeepAliveWithoutCalls(true)
                /**
                 * DEFAULT_SERVER_KEEPALIVE_TIMEOUT_NANOS = TimeUnit.SECONDS.toNanos(20L);
                 * 默认值为20秒
                 */
                .permitKeepAliveTime(10, TimeUnit.SECONDS) //10秒的可以避免触发too_many_pings异常！！
                /**
                 * keepAliveTime与keepAliveTimeout两个配置不建议在服务器端使用
                 * 如果是双向流的模式可以使用，作用是连接探活
                 */
                //.keepAliveTime(1, TimeUnit.SECONDS)
                //.keepAliveTimeout(1, TimeUnit.NANOSECONDS)
                .addService(new EchoGreeterServiceImpl())
                .build();
        server.start();
        log.info("Start server port[{}]", port);
        server.awaitTermination();
    }

    /**
     * 线程个数不限制，可以无限PING。
     * permitKeepAliveTime(10, TimeUnit.SECONDS)，避免触发too_many_pings问题
     */
    @Test
    @SneakyThrows
    public void keepaliveWithExecutorTest() {
        Server server = NettyServerBuilder.forPort(port)
                .intercept(new ServerTracingLogInterceptor())
                //只用一个线程：然后在当前线程产生阻塞，模拟服务处理缓慢
                //.executor(Executors.newFixedThreadPool(10))
                /**
                 * too_many_pings:Sent GOAWAY
                 * 在规定的时间内发送了超过2次ping请求
                 */
                .permitKeepAliveWithoutCalls(true)
                /**
                 * DEFAULT_SERVER_KEEPALIVE_TIMEOUT_NANOS = TimeUnit.SECONDS.toNanos(20L);
                 * 默认值为20秒
                 */
                .permitKeepAliveTime(10, TimeUnit.SECONDS)
                /**
                 * keepAliveTime与keepAliveTimeout两个配置不建议在服务器端使用
                 * 如果是双向流的模式可以使用，作用是连接探活
                 */
                //.keepAliveTime(1, TimeUnit.SECONDS)
                //.keepAliveTimeout(1, TimeUnit.NANOSECONDS)
                .addService(new EchoGreeterServiceImpl())
                .build();
        server.start();
        log.info("Start server port[{}]", port);
        server.awaitTermination();
    }

    /**
     * 只使用10个线程
     */
    @Test
    @SneakyThrows
    public void keepaliveWithExecutorWith10ThreadsTest() {
        Server server = NettyServerBuilder.forPort(port)
                .intercept(new ServerTracingLogInterceptor())
                //只用一个线程：然后在当前线程产生阻塞，模拟服务处理缓慢
                .executor(Executors.newFixedThreadPool(10))
                /**
                 * too_many_pings:Sent GOAWAY
                 * 在规定的时间内发送了超过2次ping请求
                 */
                .permitKeepAliveWithoutCalls(true)
                /**
                 * DEFAULT_SERVER_KEEPALIVE_TIMEOUT_NANOS = TimeUnit.SECONDS.toNanos(20L);
                 * 默认值为20秒
                 */
                .permitKeepAliveTime(10, TimeUnit.SECONDS)
                /**
                 * keepAliveTime与keepAliveTimeout两个配置不建议在服务器端使用
                 * 如果是双向流的模式可以使用，作用是连接探活
                 */
                //.keepAliveTime(1, TimeUnit.SECONDS)
                //.keepAliveTimeout(1, TimeUnit.NANOSECONDS)
                .addService(new EchoGreeterServiceImpl())
                .build();
        server.start();
        log.info("Start server port[{}]", port);
        server.awaitTermination();
    }

    /**
     * permitKeepAliveTime时间范围为默认值5分钟
     */
    @Test
    @SneakyThrows
    public void keepalivePermitKeepAliveTimeWithExecutorTest() {
        Server server = NettyServerBuilder.forPort(port)
                .intercept(new ServerTracingLogInterceptor())
                //只用一个线程：然后在当前线程产生阻塞，模拟服务处理缓慢
                .executor(Executors.newFixedThreadPool(10))
                /**
                 * too_many_pings:Sent GOAWAY
                 * 在规定的时间内发送了超过2次ping请求
                 */
                .permitKeepAliveWithoutCalls(true)
                /**
                 * DEFAULT_SERVER_KEEPALIVE_TIMEOUT_NANOS = TimeUnit.SECONDS.toNanos(20L);
                 * 默认值为20秒
                 */
                .permitKeepAliveTime(5, TimeUnit.MINUTES) //permitKeepAliveTime默认值为5分钟
                /**
                 * keepAliveTime与keepAliveTimeout两个配置不建议在服务器端使用
                 * 如果是双向流的模式可以使用，作用是连接探活
                 */
                //.keepAliveTime(1, TimeUnit.SECONDS)
                //.keepAliveTimeout(1, TimeUnit.NANOSECONDS)
                .addService(new EchoGreeterServiceImpl())
                .build();
        server.start();
        log.info("Start server port[{}]", port);
        server.awaitTermination();
    }

    /**
     * 设置permitKeepAliveWithoutCalls(false)
     * 设置是否允许客户端发送保持连接的HTTP/2 ping，即使没有未完成的
     * 简单说：是否允许客户端在没有请求时，发送ping请求来保持连接
     */
    @Test
    @SneakyThrows
    public void keepaliveNoPermitKeepAliveTimeWithExecutorTest() {
        Server server = NettyServerBuilder.forPort(port)
                .intercept(new ServerTracingLogInterceptor())
                //只用一个线程：然后在当前线程产生阻塞，模拟服务处理缓慢
                //.executor(Executors.newFixedThreadPool(10))
                /**
                 * too_many_pings:Sent GOAWAY
                 * 在规定的时间内发送了超过2次ping请求
                 *
                 * 设置是否允许客户端发送保持连接的HTTP/2 ping，即使没有未完成的
                 */
                .permitKeepAliveWithoutCalls(false)
                /**
                 * DEFAULT_SERVER_KEEPALIVE_TIMEOUT_NANOS = TimeUnit.SECONDS.toNanos(20L);
                 * 默认值为20秒
                 */
                .permitKeepAliveTime(5, TimeUnit.MINUTES) //permitKeepAliveTime默认值为5分钟
                /**
                 * keepAliveTime与keepAliveTimeout两个配置不建议在服务器端使用
                 * 如果是双向流的模式可以使用，作用是连接探活
                 */
                //.keepAliveTime(1, TimeUnit.SECONDS)
                //.keepAliveTimeout(1, TimeUnit.NANOSECONDS)
                .addService(new EchoGreeterServiceImpl())
                .build();
        server.start();
        log.info("Start server port[{}]", port);
        server.awaitTermination();
    }

    /**
     * 允许服务器接受任意数量的ping,配置如下：
     * .permitKeepAliveWithoutCalls(true)
     * .permitKeepAliveTime(10, TimeUnit.SECONDS)
     */
    @Test
    @SneakyThrows
    public void keepaliveNoTooManyPingsWithExecutorTest() {
        Server server = NettyServerBuilder.forPort(port)
                .intercept(new ServerTracingLogInterceptor())
                //只用一个线程：然后在当前线程产生阻塞，模拟服务处理缓慢
                //.executor(Executors.newFixedThreadPool(10))
                /**
                 * too_many_pings:Sent GOAWAY
                 * 在规定的时间内发送了超过2次ping请求
                 *
                 * 设置是否允许客户端发送保持连接的HTTP/2 ping，即使没有未完成的
                 * 简单说：是否允许客户端在没有请求时，发送ping请求来保持连接
                 */
                .permitKeepAliveWithoutCalls(true)
                /**
                 * DEFAULT_SERVER_KEEPALIVE_TIMEOUT_NANOS = TimeUnit.SECONDS.toNanos(20L);
                 * 默认值为20秒
                 */
                .permitKeepAliveTime(10, TimeUnit.SECONDS) //允许服务器接受任意数量的ping,避免触发too_many_ping问题
                /**
                 * keepAliveTime与keepAliveTimeout两个配置不建议在服务器端使用
                 * 如果是双向流的模式可以使用，作用是连接探活
                 */
                //.keepAliveTime(1, TimeUnit.SECONDS)
                //.keepAliveTimeout(1, TimeUnit.NANOSECONDS)
                .addService(new EchoGreeterServiceImpl())
                .build();
        server.start();
        log.info("Start server port[{}]", port);
        server.awaitTermination();
    }

    /**
     * 启动一组后端服务
     */
    @Test
    @SneakyThrows
    public void serverGroupKeepaliveNoTooManyPingsWithExecutorTest() {
        for (int i = 0; i < serverCount; i++) {
            int newPort = port + i;
            Server server = NettyServerBuilder.forPort(newPort)
                    .intercept(new ServerTracingLogInterceptor())
                    //只用一个线程：然后在当前线程产生阻塞，模拟服务处理缓慢
                    //.executor(Executors.newFixedThreadPool(10))
                    /**
                     * too_many_pings:Sent GOAWAY
                     * 在规定的时间内发送了超过2次ping请求
                     *
                     * 设置是否允许客户端发送保持连接的HTTP/2 ping，即使没有未完成的
                     * 简单说：是否允许客户端在没有请求时，发送ping请求来保持连接
                     */
                    .permitKeepAliveWithoutCalls(true)
                    /**
                     * DEFAULT_SERVER_KEEPALIVE_TIMEOUT_NANOS = TimeUnit.SECONDS.toNanos(20L);
                     * 默认值为20秒
                     */
                    .permitKeepAliveTime(10, TimeUnit.SECONDS) //允许服务器接受任意数量的ping,避免触发too_many_ping问题
                    /**
                     * keepAliveTime与keepAliveTimeout两个配置不建议在服务器端使用
                     * 如果是双向流的模式可以使用，作用是连接探活
                     */
                    //.keepAliveTime(1, TimeUnit.SECONDS)
                    //.keepAliveTimeout(1, TimeUnit.NANOSECONDS)
                    .addService(new EchoGreeterServiceImpl())
                    .build();
            server.start();
            log.info("Start server port[{}]", newPort);
            //server.awaitTermination();
        }
        Thread.currentThread().join();
    }
}
