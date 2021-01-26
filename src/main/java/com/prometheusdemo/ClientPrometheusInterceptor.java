package com.prometheusdemo;

import cn.hutool.core.net.NetUtil;
import com.GrpcUtil;
import com.opentracinglog.RoutingLog;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;

import static com.StringConstant.HYPHEN;
import static com.opentracinglog.RoutingLog.SUCCESS_STATUS;
import static com.opentracinglog.RoutingLogConstant.ROLE_CLIENT;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class ClientPrometheusInterceptor implements ClientInterceptor {
    private final RoutingPrometheusMetrics routingPrometheusMetrics;

    public ClientPrometheusInterceptor(RoutingPrometheusMetrics routingPrometheusMetrics) {
        this.routingPrometheusMetrics = routingPrometheusMetrics;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        RoutingLog routingLog = new RoutingLog();
        routingLog.setRole(ROLE_CLIENT);
        String authority = next.authority() == null ? HYPHEN : next.authority();
        routingLog.setHost(authority);
        //缺陷1：目前无法获取到服务端真实IP，暂时只能使用host来代替。
        routingLog.setTargetIp(authority);
        //缺陷2：当本机存在2张网卡的时候，本机IP设置不一定准确。
        routingLog.setEntryIp(NetUtil.getLocalhost().getHostAddress());
        routingLog.setUri(method.getFullMethodName());
        routingLog.setGrpcService(GrpcUtil.getGrpcService(method.getFullMethodName()));
        routingLog.setRequestType(method.getType() == null ? HYPHEN : method.getType().toString());
        return new TracingSimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions), routingLog);
    }

    public class TracingSimpleForwardingClientCall<ReqT, RespT> extends ForwardingClientCall<ReqT, RespT> {

        private final ClientCall<ReqT, RespT> delegate;
        private final RoutingLog routingLog;

        protected TracingSimpleForwardingClientCall(ClientCall<ReqT, RespT> delegate, RoutingLog routingLog) {
            log.info("[NEW_CLIENT_CALL]:starting");
            this.delegate = delegate;
            this.routingLog = routingLog;
        }

        @Override
        protected ClientCall<ReqT, RespT> delegate() {
            return delegate;
        }

        @Override
        public void start(Listener<RespT> responseListener, Metadata headers) {
            Listener<RespT> tracingLogResponseListener = new ForwardingClientCallListener
                    .SimpleForwardingClientCallListener<RespT>(responseListener) {
                @Override
                public void onHeaders(Metadata headers) {
                    log.info("Response headers received:[{}].", headers.toString());
                    delegate().onHeaders(headers);
                }

                @Override
                public void onMessage(RespT message) {
                    log.info("Response received.");
                    delegate().onMessage(message);
                }

                @Override
                public void onClose(Status status, Metadata trailers) {
                    log.info("Call closed!");
                    if (status == Status.OK) {
                        routingLog.setTargetResponseCode(SUCCESS_STATUS);
                    } else {
                        routingLog.setTargetResponseCode(status.getCode().value());
                        routingLog.setInterruptMessage(status.getCause() == null ?
                                status.getDescription() : String.valueOf(status.getCause()));
                    }
                    routingLog.finish();
                    routingPrometheusMetrics.incrementRequestCounter(routingLog.getGrpcService(),
                            String.valueOf(routingLog.getInnerResponseCode()),
                            String.valueOf(routingLog.getTargetResponseCode()));
                    routingPrometheusMetrics.changeRequestLatencyHistogram(routingLog.getGrpcService(),
                            ROLE_CLIENT, routingLog.getTotalCost());
                    delegate().onClose(status, trailers);
                }
            };
            delegate().start(tracingLogResponseListener, headers);
        }

        @Override
        public void cancel(@Nullable String message, @Nullable Throwable cause) {
            log.error("[TRACING_LOG_CATCH_EXCEPTION]:traceId[{}]", routingLog.getTraceId(), cause);
            delegate().cancel(message, cause);
        }

        @Override
        public void halfClose() {
            log.info("Finished sending messages.");
            delegate().halfClose();
        }

        @Override
        public void sendMessage(ReqT message) {
            log.info("Sending messages.");
            delegate().sendMessage(message);
        }
    }

}
