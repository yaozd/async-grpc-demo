package com.mattie.demo;

import com.mattie.grpc.GreeterGrpc;
import com.mattie.grpc.HelloWorldProtos;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;

/**
 * @Author: yaozh
 * @Description:
 */
public class BlockClientSetHeaderTest {
    /**
     * 设置grpc请求头demo
     * @param args
     */
    public static void main(String[] args) {
        ManagedChannel channel = NettyChannelBuilder.forAddress("localhost", 50050)
                .usePlaintext(true)
                .build();
        GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(channel);
        Metadata header=new Metadata();
        Metadata.Key<String> key =
                Metadata.Key.of("shopid", Metadata.ASCII_STRING_MARSHALLER);
        header.put(key, "100001");
        stub = MetadataUtils.attachHeaders(stub, header);
        HelloWorldProtos.HelloReply helloReply = stub.
                sayHello(HelloWorldProtos.HelloRequest.newBuilder().setMessage("hello wolrd").build());
        System.out.println("reply:" + helloReply.getMessage());

    }
}
