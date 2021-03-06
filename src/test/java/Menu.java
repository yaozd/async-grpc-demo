/**
 * 测试功能目录
 *
 * @Author: yaozh
 * @Description:
 * @see com.GrpcUtil
 * {@link com.GrpcUtil}
 * 推荐使用@link，因为@link可以放在任何地方
 */
public enum Menu {
    /**
     * TOKEN认证测试
     * {@link org.nuhara.demos.AuthGrpcHeaderTest#oneCallTest()}
     */
    AUTH_CLIENT_TEST,
    /**
     * GRPC请求跟踪
     * {@link com.opentracinglog}
     */
    OPENTRACING_TEST,
    /**
     * GRPC keepalive
     * {@link com.keepalive}
     */
    KEEPALIVE_TEST,
    /**
     * 模拟耗时请求，触发网关timeout
     * 1.
     * client
     * {@link com.longtime.TimeoutClient}
     * 2.
     * server
     * {@link com.longtime.TimeoutServer}
     */
    TIMEOUT_TEST
}
