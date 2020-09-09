package com.mattie.demo;

import com.google.common.annotations.VisibleForTesting;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ethan
 */
@Slf4j
public class HeaderServerInterceptor implements ServerInterceptor {

    //VisibleForTesting的注解来提醒其他程序员: 这里为了测试私有方法把私有方法改成了Protected(受保护的)并放宽了访问限制
    @VisibleForTesting
    static final Metadata.Key<String> CUSTOM_HEADER_KEY =
            Metadata.Key.of("groupid", Metadata.ASCII_STRING_MARSHALLER);


    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            final Metadata requestHeaders,
            ServerCallHandler<ReqT, RespT> next) {
        log.info("header received from client:" + requestHeaders);
        String groupId = requestHeaders.get(CUSTOM_HEADER_KEY);
        //获取grpc请求host
        String authority = call.getAuthority();
        log.info(authority);
        log.info("receive groupId:"+groupId);
        //模拟异常
        if("68".equalsIgnoreCase(groupId)){
            call.close(Status.UNAUTHENTICATED.withDescription("{\"msg\":not authorized}"), requestHeaders);
            return new ServerCall.Listener<ReqT>() {};
        }
        if("88".equalsIgnoreCase(groupId)){
            throw Status.INTERNAL.withDescription("More than one value received for unary call").asRuntimeException();
        }
        return next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
            @Override
            public void sendHeaders(Metadata responseHeaders) {
                //responseHeaders.put(CUSTOM_HEADER_KEY, requestHeaders.get(CUSTOM_HEADER_KEY));
                super.sendHeaders(responseHeaders);
            }
        }, requestHeaders);
    }
}