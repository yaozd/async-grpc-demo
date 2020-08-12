package io.grpc.benchmarks.connection;

import com.google.protobuf.ByteString;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.benchmarks.proto.Messages;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * grpc-连接数测试：1万长连接的是否工作正常
 *
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class ConnectionTest {
    private static Messages.PayloadType payloadType = Messages.PayloadType.COMPRESSABLE;
    //private static int clientPayload = 100000000;
    private static int clientPayload = 1;
    private static int serverPayload = clientPayload;
    private static AtomicInteger failCounter = new AtomicInteger();
    private static int nThreads = 500;
    private static CountDownLatch downLatch = new CountDownLatch(nThreads);

    public static void main(String[] args) {
        //循环测试连接-故意产生异常与连接断开，查看AR是否可以正常回收资源
        for (int i = 0; i <nThreads; i++) {
            log.error("i="+i);
            try{
                createConnection();
            }catch (Throwable throwable){

            }
        }
    }

    private static void createConnection() {
        Messages.SimpleRequest request = newRequest();
        List<Channel> channelList = new ArrayList<>();
        for (int i = 0; i < nThreads; i++) {
            try {
                //channelList.add(newChannel("192.168.56.102:8888"));
                //channelList.add(newChannel("127.0.0.1:8888"));
                channelList.add(newChannel("127.0.0.1:50051"));
                boolean isAwait = await(i, 10);
                if (isAwait) {
                    for (Channel channel : channelList) {
                        new GrpcClient().doUnaryCalls(channel, request);
                    }
                }
            } catch (Exception ex) {
                log.error("Occur Exception!", ex);
            }
        }
        for (Channel channel : channelList) {
            new GrpcClient().doUnaryCalls(channel, request);
        }
        log.info("done");
        for (Channel channel : channelList) {
            ManagedChannel managedChannel = (ManagedChannel) channel;
            managedChannel.shutdownNow();
        }
    }

    private static boolean await(int i, int channelNum) {
        if (i % channelNum == 0) {
            int n = 2 * 1000;
            log.info("channels:{},sleep:{} ms", i, n);
            try {
                Thread.sleep(n);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    private static ManagedChannel newChannel(String target) {
        return NettyChannelBuilder.forTarget(target)
                .eventLoopGroup(new NioEventLoopGroup(1))
                .directExecutor()
                //.disableRetry()
                .keepAliveWithoutCalls(true)
                .keepAliveTime(1000, TimeUnit.MINUTES)
                //.flowControlWindow(Integer.MAX_VALUE)
                //.maxInboundMessageSize(20971520)
                //channel 长连接时间
                .keepAliveTimeout(1000, TimeUnit.MINUTES)
                //channel 空闲超时时间
                .idleTimeout(1000, TimeUnit.MINUTES)
                .usePlaintext().build();
    }

    private static Messages.SimpleRequest newRequest() {
        ByteString body = ByteString.copyFrom(new byte[clientPayload]);
        Messages.Payload payload = Messages.Payload.newBuilder().setType(payloadType).setBody(body).build();

        return Messages.SimpleRequest.newBuilder()
                .setResponseType(payloadType)
                .setResponseSize(serverPayload)
                .setPayload(payload)
                .build();
    }
}
