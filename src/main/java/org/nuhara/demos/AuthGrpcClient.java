package org.nuhara.demos;

import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.channel.nio.NioEventLoopGroup;
import org.nuhara.model.proto.HelloGrpc;

import java.util.concurrent.TimeUnit;

/**
 * @Author: yaozh
 * @Description:
 */
public class AuthGrpcClient {
    private static final int MAX_INDEX = 100_000_000;
    private static final NioEventLoopGroup EVENT_LOOP_GROUP = new NioEventLoopGroup(1);
    private final int num;
    private final ManagedChannel[] channels;
    private final HelloGrpc.HelloStub[] stubs;
    private int index = 0;

    public AuthGrpcClient(int num) {
        if (num < 1) {
            num = 1;
        }
        this.num = num;
        this.channels = new ManagedChannel[num];
        this.stubs = new HelloGrpc.HelloStub[num];
        init();
    }

    private void init() {
        for (int i = 0; i < num; i++) {
            ManagedChannel channel = createChannel();
            channels[i] = channel;
            stubs[i] = HelloGrpc.newStub(channel);
        }
    }

    /**
     * @return
     */
    public HelloGrpc.HelloStub getStub() {
        if (num == 1) {
            return stubs[0];
        }
        if (index > MAX_INDEX) {
            index = 0;
        }
        index++;
        //System.out.println(index % num);
        return stubs[index % num];
    }

    private ManagedChannel createChannel() {
        return NettyChannelBuilder.forTarget("localhost:50051")
                //此方案增加了上下文切换，十分耗时，不可取
                //.eventLoopGroup(new NioEventLoopGroup(1))
                //推荐共享模式
                .eventLoopGroup(EVENT_LOOP_GROUP)
                .directExecutor()
                //.executor(newThreadPoolExecutor())
                .disableRetry()
                .maxInboundMessageSize(20971520)
                .keepAliveTimeout(1, TimeUnit.SECONDS)
                .idleTimeout(1, TimeUnit.SECONDS)
                .usePlaintext().build();
    }
}
