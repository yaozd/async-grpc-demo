package org.nuhara.demos;

import com.mattie.demo.MyService;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class GreeterServer {
    private static final int port = 8899;

    @Test
    public void testGreeterServer() throws IOException, InterruptedException {
        NettyServerBuilder serverBuilder = NettyServerBuilder.forPort(port);
        //serverBuilder.intercept(new HeaderServerInterceptor());
        //serverBuilder.addService(ServerInterceptors.intercept(new MyService(), new HeaderServerInterceptor()));
        serverBuilder.addService(new MyService());
        //serverBuilder.flowControlWindow(1000000000);
        //模拟PING
        //serverBuilder.permitKeepAliveWithoutCalls(true);
        //serverBuilder.keepAliveTime(3,TimeUnit.SECONDS);
        //PING
        serverBuilder.maxInboundMessageSize(1024 * 1024 * 20);
        serverBuilder.handshakeTimeout(10, TimeUnit.SECONDS);
        Server server = serverBuilder.build();
        server.start();
        server.awaitTermination();
    }
}
