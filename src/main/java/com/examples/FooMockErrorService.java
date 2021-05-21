package com.examples;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

/**
 * GRPC的四种服务类型
 * https://www.cnblogs.com/resentment/p/6792029.html
 * GRPC错误处理
 * https://www.cnblogs.com/resentment/p/6883153.html
 *
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class FooMockErrorService extends FooGrpc.FooImplBase {

    /**
     * 简单rpc
     * 这就是一般的rpc调用，一个请求对象对应一个返回对象
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void simpleHello(FooProto.FooRequest request,
                            StreamObserver<FooProto.FooResponse> responseObserver) {
        //
        log.info(request.getCode() + " calling");
        //返回一个包装成Exception的Status来返回错误信息，如果直接使用Throwable，客户端无法获得错误信息
        responseObserver.onError(Status.INTERNAL.withDescription("error desc").asRuntimeException());
        //如果调用了onError会自动complete无需手动complete
        //responseObserver.onCompleted();
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
                //返回异常
                //responseObserver.onError(Status.INTERNAL.withDescription("error desc").asRuntimeException());
                //responseObserver.onNext(builder.build());
                //responseObserver.onCompleted();
                responseObserver.onError(Status.CANCELLED.asRuntimeException());
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
