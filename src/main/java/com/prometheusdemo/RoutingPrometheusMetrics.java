package com.prometheusdemo;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;

import java.util.concurrent.TimeUnit;

/**
 * 参考：API-ROUTER的监控指标
 *
 * @Author: yaozh
 * @Description:
 */
class RoutingPrometheusMetrics {

    private static final String REQUEST_COUNTER_METRICS = "routing_requests_total";
    private static final String EXCEPTION_METRICS = "routing_exception_total";
    private static final String REQUEST_LATENCY_METRICS = "routing_request_seconds";

    private static final String SERVICE_NAME_TAG = "service";
    private static final String INNER_STATUS_TAG = "inner_status";
    private static final String TARGET_STATUS_TAG = "target_status";
    private static final String ENDPOINT_TYPE_TAG = "endpoint_type";
    private final Counter requestCounter;
    private final Counter exceptionCounter;
    private final Histogram requestLatencyHistogram;

    public RoutingPrometheusMetrics() {
        this.requestCounter = Counter.build().name(REQUEST_COUNTER_METRICS)
                .help("Routing:total requests.")
                .labelNames(SERVICE_NAME_TAG, INNER_STATUS_TAG, TARGET_STATUS_TAG)
                .register();
        this.exceptionCounter = Counter.build().name(EXCEPTION_METRICS)
                .help("Routing: total errors.")
                .labelNames(SERVICE_NAME_TAG, ENDPOINT_TYPE_TAG)
                .register();
        this.requestLatencyHistogram = Histogram.build()
                .buckets(0.1, 0.5, 1, 5)
                .name(REQUEST_LATENCY_METRICS)
                .help("Routing: request latency in seconds")
                .labelNames(SERVICE_NAME_TAG, ENDPOINT_TYPE_TAG)
                .register();
    }

    public void incrementRequestCounter(String serviceName, String innerStatus, String targetStatus) {
        requestCounter.labels(serviceName, innerStatus, targetStatus).inc();
    }

    public void incrementException(String serviceName, String endpointType) {
        exceptionCounter.labels(serviceName, endpointType).inc();
    }

    public void changeRequestLatencyHistogram(String serviceName, String endpointType, long latency) {
        requestLatencyHistogram.labels(serviceName, endpointType).observe(TimeUnit.MILLISECONDS.toSeconds(latency));
    }
}
