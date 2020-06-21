package com.resolver;

import com.mattie.grpc.GreeterGrpc;
import com.mattie.grpc.HelloWorldProtos;
import io.grpc.*;
import io.grpc.internal.DnsNameResolverProvider;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
/**
 * java grpc client集成consul服务发现
 * https://www.jianshu.com/p/997505834bf5
 */
public class GrpcClient {
    public static void main(String[] args) {
        /**
         * 服务器端：
         * com.mattie.demo.StreamServer
         */
        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder
                .forTarget("localhost:8899")
                //.nameResolverFactory(new DnsNameResolverProvider())
                .nameResolverFactory(new NameResolver.Factory() {
                    @Nullable
                    @Override
                    public NameResolver newNameResolver(URI uri, Attributes attributes) {
                        return new NameResolver() {

                            @Override
                            public String getServiceAuthority() {
                                return uri.getAuthority()==null?String.valueOf(uri):uri.getAuthority();
                            }

                            /**
                             * 更新服务器地址：listener.onAddresses
                             * @param listener
                             */
                            @Override
                            public void start(Listener listener) {
                                List<EquivalentAddressGroup> servers=new ArrayList<>();
                                servers.add(new EquivalentAddressGroup((new InetSocketAddress("localhost", 8899))));
                                //Handles updates on resolved addresses and attributes.
                                listener.onAddresses(servers,Attributes.EMPTY);
                            }

                            @Override
                            public void shutdown() {

                            }
                        };
                    }

                    @Override
                    public String getDefaultScheme() {
                        return "k8s|etcd|consul";
                    }
                })
                .usePlaintext();
        ManagedChannel channel = channelBuilder.build();
        GreeterGrpc.GreeterBlockingStub blockingStub = GreeterGrpc.newBlockingStub(channel);
        HelloWorldProtos.HelloReply helloReply = blockingStub.
                sayHello(HelloWorldProtos.HelloRequest.newBuilder().setMessage("hello wolrd").build());
        System.out.println(helloReply.getMessage());
    }

}
