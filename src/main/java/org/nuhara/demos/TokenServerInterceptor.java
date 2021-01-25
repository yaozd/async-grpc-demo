package org.nuhara.demos;


import io.grpc.*;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: yaozh
 * @Description:
 */
public class TokenServerInterceptor implements ServerInterceptor {
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
                                                                 Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> next) {
        String token = headers.get(Metadata.Key.of("token", Metadata.ASCII_STRING_MARSHALLER));
        boolean isOk = StringUtils.isNoneBlank(token);
        if (isOk) {
            return next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
            }, headers);
        }
        call.close(Status.UNAUTHENTICATED.withDescription("not found token"), headers);
        return new ServerCall.Listener<ReqT>() {
        };
    }
}
