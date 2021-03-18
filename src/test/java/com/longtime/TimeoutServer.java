package com.longtime;

import com.echo.EchoGreeterServiceImpl;
import com.opentracinglog.ServerTracingLogInterceptor;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class TimeoutServer {
    /**
     * 模拟触发TIMEOUT操作场景，推荐使用directExecutor模式
     */
    @Test
    @SneakyThrows
    public void runTimeoutServerWithDirectExecutor() {
        int port=8899;
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
}
