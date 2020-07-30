package io.grpc.benchmarks.connection;

import io.grpc.Channel;
import io.grpc.Status;
import io.grpc.benchmarks.proto.BenchmarkServiceGrpc;
import io.grpc.benchmarks.proto.Messages;
import io.grpc.stub.StreamObserver;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class GrpcClient {
    public void doUnaryCalls(Channel channel,Messages.SimpleRequest req) {
        final BenchmarkServiceGrpc.BenchmarkServiceStub stub = BenchmarkServiceGrpc.newStub(channel);
        //
        stub.unaryCall(req, new StreamObserver<Messages.SimpleResponse>() {

            @SneakyThrows
            @Override
            public void onNext(Messages.SimpleResponse simpleResponse) {
                //log.info("ok");
            }

            @Override
            public void onError(Throwable throwable) {
                Status status = Status.fromThrowable(throwable);
                System.err.println("Encountered an error in unaryCall. Status is " + status);
                throwable.printStackTrace();
            }

            @Override
            public void onCompleted() {
            }
        });
    }
}
