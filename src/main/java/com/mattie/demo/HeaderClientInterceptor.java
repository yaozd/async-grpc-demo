package com.mattie.demo;

import com.google.common.annotations.VisibleForTesting;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ethan
 */
@Slf4j
public class HeaderClientInterceptor implements ClientInterceptor {

    AtomicInteger index = new AtomicInteger();

    //VisibleForTesting的注解来提醒其他程序员: 这里为了测试私有方法把私有方法改成了Protected(受保护的)并放宽了访问限制
    @VisibleForTesting
    static final Metadata.Key<String> CUSTOM_HEADER_KEY =
            Metadata.Key.of("groupID", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                               CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                String groupID = "58";
                if (index.incrementAndGet() % 10 == 0) {
                    groupID = "68";
                }
                if (index.incrementAndGet() % 5 == 0) {
                    groupID = "88";
                }
//                log.info("header group id :" + groupID);
                /* put custom header */
                headers.put(CUSTOM_HEADER_KEY, groupID);
                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    @Override
                    public void onHeaders(Metadata headers) {
                        /**
                         * if you don't need receive header from server,
                         * you can use {@link io.grpc.stub.MetadataUtils#attachHeaders}
                         * directly to send header
                         */
                        log.info("header received from server:" + headers);
                        super.onHeaders(headers);
                    }
                }, headers);
            }
        };
    }
}
