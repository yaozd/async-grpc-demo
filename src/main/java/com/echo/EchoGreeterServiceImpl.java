package com.echo;

import cn.hutool.core.thread.ThreadUtil;
import com.DataUtil;
import com.echo.grpc.EchoGreeterGrpc.EchoGreeterImplBase;
import com.echo.grpc.EchoGreeterProto;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class EchoGreeterServiceImpl extends EchoGreeterImplBase {
    @Override
    public void sayHello(EchoGreeterProto.EchoRequest request,
                         StreamObserver<EchoGreeterProto.EchoReply> responseObserver) {
        //模拟：业务处理时长
        if (request.getSleepMills() > 0L) {
            log.info("[START_SLEEP]Sleep time milliseconds:[{}]", request.getSleepMills());
            ThreadUtil.sleep(request.getSleepMills(), TimeUnit.MILLISECONDS);
            log.info("[END_SLEEP]");
        }
        //ThreadUtil.sleep(600, TimeUnit.SECONDS);
        responseObserver.onNext(newReplyData(request.getSize()));
        responseObserver.onCompleted();
    }

    private EchoGreeterProto.EchoReply newReplyData(long size) {
        return EchoGreeterProto.EchoReply.newBuilder()
                .setMessage(DataUtil.getMockData((int) size))
                .setAtTime(System.currentTimeMillis())
                .build();
    }

    @Override
    public StreamObserver<EchoGreeterProto.EchoStreamRequest> biStream(
            StreamObserver<EchoGreeterProto.EchoStreamResponse> responseObserver) {
        return super.biStream(responseObserver);
    }
}
