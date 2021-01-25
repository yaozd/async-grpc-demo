package com.examples.Interceptor;

import io.grpc.*;

/**
 * GRPC拦截器
 * https://www.cnblogs.com/resentment/p/6818753.html
 * @Author: yaozh
 * @Description:
 */
public class MyServerInterceptor implements ServerInterceptor {
    //服务端header的key
    static final Metadata.Key<String> CUSTOM_HEADER_KEY =
            Metadata.Key.of("serverHeader", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        //输出客户端传递过来的header
        System.out.println("header received from client:" + headers);

        return next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
            @Override
            public void sendHeaders(Metadata responseHeaders) {
                //在返回中增加header
                responseHeaders.put(CUSTOM_HEADER_KEY, "response");
                super.sendHeaders(responseHeaders);
            }
        }, headers);
    }
}