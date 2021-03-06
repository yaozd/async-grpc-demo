package com.prometheusdemo;

import com.GrpcUtil;
import com.opentracinglog.RoutingLog;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

import static com.opentracinglog.RoutingLog.SUCCESS_STATUS;
import static com.opentracinglog.RoutingLogConstant.ROLE_CLIENT;
import static com.opentracinglog.RoutingLogConstant.ROLE_SERVER;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class ServerPrometheusInterceptor implements ServerInterceptor {
    private final RoutingPrometheusMetrics routingPrometheusMetrics;

    public ServerPrometheusInterceptor(RoutingPrometheusMetrics routingPrometheusMetrics) {
        this.routingPrometheusMetrics = routingPrometheusMetrics;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
                                                                 Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> next) {
        RoutingLog routingLog = new RoutingLog();
        routingLog.setRole(ROLE_SERVER);
        routingLog.setHost(call.getAuthority());
        call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        routingLog.setTargetIp(call.getAttributes().get(Grpc.TRANSPORT_ATTR_LOCAL_ADDR).toString());
        routingLog.setEntryIp(call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR).toString());
        routingLog.setUri(call.getMethodDescriptor().getFullMethodName());
        routingLog.setGrpcService(GrpcUtil.getGrpcService(call.getMethodDescriptor().getFullMethodName()));
        routingLog.setRequestType(call.getMethodDescriptor().getType().toString());
        ServerCall<ReqT, RespT> wrappedCall = new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
            @Override
            public void sendMessage(RespT message) {
                super.sendMessage(message);
            }

            @Override
            public void close(Status status, Metadata trailers) {
                log.info("Call closed!");
                if (status == Status.OK) {
                    routingLog.setInnerResponseCode(SUCCESS_STATUS);
                } else {
                    routingLog.setInnerResponseCode(status.getCode().value());
                    routingLog.setInterruptMessage(String.valueOf(status.getCause()));
                    log.error("[TRACING_LOG_CATCH_EXCEPTION]:traceId[{}]", routingLog.getTraceId(), status.getCause());
                }
                //routingLog.finish();
                super.close(status, trailers);
            }
        };
        ServerCall.Listener<ReqT> listenerWithContext = Contexts
                .interceptCall(Context.current(), wrappedCall, headers, next);
        return new TracingSimpleForwardingServerCallListener<ReqT>(listenerWithContext, routingLog);
    }

    public class TracingSimpleForwardingServerCallListener<ReqT>
            extends ForwardingServerCallListener<ReqT> {

        private final ServerCall.Listener<ReqT> delegate;
        private final RoutingLog routingLog;

        protected TracingSimpleForwardingServerCallListener(ServerCall.Listener<ReqT> delegate, RoutingLog routingLog) {
            log.info("[NEW_CLIENT_CALL]:starting");
            this.delegate = delegate;
            this.routingLog = routingLog;
        }

        @Override
        protected ServerCall.Listener<ReqT> delegate() {
            return delegate;
        }

        @Override
        public void onMessage(ReqT message) {
            log.info("Message received:[{}]", message);
            delegate().onMessage(message);
        }

        @Override
        public void onHalfClose() {
            log.info("Finished sending messages");
            try {
                delegate().onHalfClose();
            } catch (Throwable ex) {
                routingLog.setInnerResponseCode(Status.UNKNOWN.getCode().value());
                routingLog.setInterruptMessage(String.valueOf(ex));
                log.error("[TRACING_LOG_CATCH_EXCEPTION]:traceId[{}]", routingLog.getTraceId(), ex);
                throw ex;
            }
        }

        /**
         * 例如：客户端设置超时，超时后会触发服务器端的Cancel操作
         */
        @Override
        public void onCancel() {
            log.info("Call cancelled");
            routingPrometheusMetrics.incrementRequestCounter(routingLog.getGrpcService(),
                    String.valueOf(routingLog.getInnerResponseCode()),
                    String.valueOf(routingLog.getTargetResponseCode()));
            routingPrometheusMetrics.changeRequestLatencyHistogram(routingLog.getGrpcService(),
                    ROLE_CLIENT, routingLog.getTotalCost());
            //给这条记录打上标签
            routingLog.setInterruptMessage(routingLog.getInterruptMessage() + "[CALL_CANCELLED]");
            routingLog.finish();
            delegate().onCancel();
        }

        @Override
        public void onComplete() {
            log.info("Call completed");
            routingPrometheusMetrics.incrementRequestCounter(routingLog.getGrpcService(),
                    String.valueOf(routingLog.getInnerResponseCode()),
                    String.valueOf(routingLog.getTargetResponseCode()));
            routingPrometheusMetrics.changeRequestLatencyHistogram(routingLog.getGrpcService(),
                    ROLE_CLIENT, routingLog.getTotalCost());
            routingLog.finish();
            delegate().onComplete();
        }
    }
}
