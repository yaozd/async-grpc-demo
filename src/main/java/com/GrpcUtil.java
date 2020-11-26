package com;

import io.grpc.Metadata;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.MetadataUtils;

import java.util.Map;

/**
 * @Author: yaozh
 * @Description:
 */
public class GrpcUtil {
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
}
