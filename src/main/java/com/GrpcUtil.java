package com;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.MetadataUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static com.StringConstant.HYPHEN;

/**
 * @Author: yaozh
 * @Description:
 */
public class GrpcUtil {
    public static final String GRPC_PATH_SEPARATOR = "/";

    public static final String GRPC_SERVICE_ROUTER_PREFIX = "grpc:";

    /**
     * gRPC请求中对header进行处理
     * https://blog.csdn.net/xuguobiao/article/details/52142337
     * stub = MetadataUtils.attachHeaders(stub, header);
     *
     * @param stub
     * @param headerMap
     * @param <T>
     * @return
     */
    public static <T extends AbstractStub<T>> T attachHeaders(T stub, final Map<String, String> headerMap) {
        Metadata extraHeaders = new Metadata();
        if (headerMap != null) {
            for (String key : headerMap.keySet()) {
                Metadata.Key<String> customHeadKey = Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER);
                extraHeaders.put(customHeadKey, headerMap.get(key));
            }
        }
        return MetadataUtils.attachHeaders(stub, extraHeaders);

    }

    /**
     * 获取grpc服务名
     *
     * @param path
     * @return
     */
    public static String getGrpcService(String path) {
        String service = StringUtils.substringBeforeLast(path, GRPC_PATH_SEPARATOR);
        if (StringUtils.isBlank(service)) {
            return HYPHEN;
        }
        return service;
    }

    /**
     * 暂时不推荐使用此方法，这个方法会抛出异常
     *
     * @param fullMethodName
     * @return
     */
    public static String extractFullServiceName(String fullMethodName) {
        return MethodDescriptor.extractFullServiceName(fullMethodName);
    }
}
