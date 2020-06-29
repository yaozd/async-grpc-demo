package org.nuhara.demos;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.logging.Logger;

public class GrpcServer {

    private static final Logger logger = Logger.getLogger(GrpcServer.class.getCanonicalName());
//	private static final Tracer tracer = Tracing.initTracer("async-grpc-demo");

    public static void main(String[] args) throws IOException, InterruptedException {

//		ServerTracingInterceptor tracingInterceptor = new ServerTracingInterceptor(tracer);
        //maxConcurrentCallsPerConnection控制并发数 streamId
        //NettyServerBuilder.forPort(8081).maxConcurrentCallsPerConnection(1);
        Server server = ServerBuilder.forPort(8181)
//				.addService(tracingInterceptor.intercept(new ISOProcessorImpl()))
                .addService(new ISOProcessorImpl())
                //设置消息的传输最大值
                .maxInboundMessageSize(20971520)
                .build();

        server.start();

        logger.info("gRPC Server Started.");

        server.awaitTermination();
    }

}
