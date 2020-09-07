package com.grpcping;

import com.demo.grpcPing.GrpcPingGrpc;
import com.demo.grpcPing.Health;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class PingService extends GrpcPingGrpc.GrpcPingImplBase {
    @Override
    public void ping(Health.PingRequest request, StreamObserver<Health.PingResponse> responseObserver) {
        log.debug("enter ping,request:{}", request);
        long now = System.currentTimeMillis();
        Health.PingResponse reply = Health.PingResponse.newBuilder().setCode(now + "").build();
        System.out.println("Reply:"+new String(reply.toByteArray(),UTF_8));
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
        log.debug("exit ping,response:{}", reply);
    }
}
