package com.examples;

import io.grpc.stub.StreamObserver;

/**
 * GRPC的四种服务类型
 * https://www.cnblogs.com/resentment/p/6792029.html
 *
 * @Author: yaozh
 * @Description:
 */
public class FooService extends FooGrpc.FooImplBase {

    /**
     * 简单rpc
     * 这就是一般的rpc调用，一个请求对象对应一个返回对象
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void simpleHello(com.examples.FooProto.FooRequest request,
                            StreamObserver<com.examples.FooProto.FooResponse> responseObserver) {
        FooProto.FooResponse fooResponse = FooProto.FooResponse.newBuilder()
                .setCode("200").build();
        responseObserver.onNext(fooResponse);
        responseObserver.onCompleted();
    }

    /**
     * 服务端流式rpc
     * 一个请求对象，服务端可以传回多个结果对象
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void serverStreamHello(FooProto.FooRequest request,
                                  StreamObserver<FooProto.FooResponse> responseObserver) {
        //返回多个结果
        responseObserver.onNext(FooProto.FooResponse.newBuilder().setCode("1").build());
        responseObserver.onNext(FooProto.FooResponse.newBuilder().setCode("2").build());
        responseObserver.onNext(FooProto.FooResponse.newBuilder().setCode("3").build());
        responseObserver.onCompleted();
    }

    /**
     * 客户端流式rpc
     * 客户端传入多个请求对象，服务端返回一个响应结果
     *
     * @param responseObserver
     * @return
     */
    @Override
    public StreamObserver<FooProto.FooRequest> clientStreamHello(StreamObserver<FooProto.FooResponse> responseObserver) {
        //返回observer应对多个请求对象
        return new StreamObserver<FooProto.FooRequest>() {
            private FooProto.FooResponse.Builder builder = FooProto.FooResponse.newBuilder();

            @Override
            public void onNext(FooProto.FooRequest value) {
                //错误写法：
                //警告: Cancelling the stream with status Status{code=INTERNAL, description=Too many responses, cause=null}
                //responseObserver.onNext(FooProto.FooResponse.newBuilder().setCode("1").build());
                //
                builder.setCode(builder.getCode() + value.getCode() + ";");
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                //responseObserver.onNext(FooProto.FooResponse.newBuilder().setCode("1").build());
                builder.setCode("SERVER:" + builder.getCode());
                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();
            }
        };
    }

    /**
     * 双向流式rpc
     * 结合客户端流式rpc和服务端流式rpc，可以传入多个对象，返回多个响应对象
     *
     * @param responseObserver
     * @return
     */
    @Override
    public StreamObserver<FooProto.FooRequest> biStreamHello(StreamObserver<FooProto.FooResponse> responseObserver) {
        //返回observer应对多个请求对象
        return new StreamObserver<FooProto.FooRequest>() {
            @Override
            public void onNext(FooProto.FooRequest value) {
                responseObserver.onNext(FooProto.FooResponse.newBuilder().setCode("1").build());
                responseObserver.onNext(FooProto.FooResponse.newBuilder().setCode("2").build());
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
