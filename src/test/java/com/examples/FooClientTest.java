package com.examples;

import cn.hutool.core.thread.ThreadUtil;
import com.ExceptionUtil;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * GRPC的四种服务类型
 * https://www.cnblogs.com/resentment/p/6792029.html
 * <p>
 * GRPC错误处理
 * https://www.cnblogs.com/resentment/p/6883153.html
 *
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class FooClientTest {
    /**
     * 简单rpc
     * 一般的rpc调用，一个请求对象对应一个返回对象
     */
    @Test
    public void simple() {

        final ManagedChannel channel = ManagedChannelBuilder.forAddress("127.0.0.1", 50051).usePlaintext().build();
        //定义同步阻塞的stub
        FooGrpc.FooBlockingStub blockingStub = FooGrpc.newBlockingStub(channel);
        FooProto.FooRequest hello = FooProto.FooRequest.newBuilder().setCode("hello").build();
        //simple
        log.info("---simple rpc---");
        log.info(blockingStub.simpleHello(hello).getCode());
        channel.shutdown();
    }

    @Test
    public void simpleWithResponseError() {
        try {
            simple();
        } catch (Exception e) {
            //将异常转换为status可以得到对应的异常信息
            Status status = Status.fromThrowable(e);
            log.info("STATUS:{}", status.getCode());
            status.asException().printStackTrace();
        }
    }

    /**
     * 服务端流式rpc
     * 一个请求对象，服务端可以传回多个结果对象
     */
    @Test
    public void serverStreamHello() {
        final ManagedChannel channel = ManagedChannelBuilder.forAddress("127.0.0.1", 50051).usePlaintext().build();
        //定义同步阻塞的stub
        FooGrpc.FooBlockingStub blockingStub = FooGrpc.newBlockingStub(channel);
        FooProto.FooRequest hello = FooProto.FooRequest.newBuilder().setCode("hello").build();
        //server side
        log.info("---server stream rpc---");
        //返回结果是Iterator
        Iterator<FooProto.FooResponse> it = blockingStub.serverStreamHello(hello);
        while (it.hasNext()) {
            log.info(it.next().getCode());
        }
        channel.shutdown();
    }

    /**
     * 客户端流式rpc
     * 客户端传入多个请求对象，服务端返回一个响应结果
     */
    @Test
    public void clientStreamHello() {
        final ManagedChannel channel = ManagedChannelBuilder.forAddress("127.0.0.1", 50051).usePlaintext().build();
        //定义异步的stub
        FooGrpc.FooStub asyncStub = FooGrpc.newStub(channel);
        //FooProto.FooRequest hello = FooProto.FooRequest.newBuilder().setCode("hello").build();
        //client side
        log.info("---client stream rpc---");
        StreamObserver<FooProto.FooResponse> responseObserver = new StreamObserver<FooProto.FooResponse>() {
            @Override
            public void onNext(FooProto.FooResponse result) {
                log.info("client stream--" + result.getCode());
            }

            @Override
            public void onError(Throwable t) {
                //处理异常
                Status status = Status.fromThrowable(t);
                status.asException().printStackTrace();
            }

            @Override
            public void onCompleted() {
                //关闭channel
                channel.shutdown();
            }
        };
        StreamObserver<FooProto.FooRequest> clientStreamObserver = asyncStub.clientStreamHello(responseObserver);
        clientStreamObserver.onNext(FooProto.FooRequest.newBuilder().setCode("hello-1").build());
        clientStreamObserver.onNext(FooProto.FooRequest.newBuilder().setCode("hello-2").build());
        clientStreamObserver.onCompleted();
        //由于是异步获得结果，所以sleep一秒
        ThreadUtil.sleep(3, TimeUnit.SECONDS);
        //channel.shutdown();
    }

    @Test
    public void biStreamHello() {
        final ManagedChannel channel = ManagedChannelBuilder.forAddress("127.0.0.1", 50051).usePlaintext().build();
        //定义异步的stub
        FooGrpc.FooStub asyncStub = FooGrpc.newStub(channel);
        //FooProto.FooRequest hello = FooProto.FooRequest.newBuilder().setCode("hello").build();
        //client side
        //bi stream
        System.out.println("---bidirectional stream rpc---");
        StreamObserver<FooProto.FooResponse> responseObserver = new StreamObserver<FooProto.FooResponse>() {
            @Override
            public void onNext(FooProto.FooResponse result) {
                //人为模拟异常
                //ExceptionUtil.mockException();
                log.info("bidirectional stream--" + result.getCode());
            }

            @Override
            public void onError(Throwable t) {
                log.error("ERROR!", t);
            }

            @Override
            public void onCompleted() {
                //关闭channel
                channel.shutdown();
            }
        };
        StreamObserver<FooProto.FooRequest> clientStreamObserver = asyncStub.biStreamHello(responseObserver);
        clientStreamObserver.onNext(FooProto.FooRequest.newBuilder().setCode("hello-1").build());
        clientStreamObserver.onNext(FooProto.FooRequest.newBuilder().setCode("hello-2").build());
        clientStreamObserver.onCompleted();
        //由于是异步获得结果，所以sleep一秒
        ThreadUtil.sleep(5, TimeUnit.SECONDS);
        //channel.shutdown();
    }
}
