package com.echo;

import cn.hutool.core.thread.ThreadUtil;
import com.DataUtil;
import com.echo.grpc.EchoGreeterGrpc.EchoGreeterImplBase;
import com.echo.grpc.EchoGreeterProto;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;

/**
 * @Author: yaozh
 * @Description:
 */
public class EchoGreeterServiceImpl extends EchoGreeterImplBase {
    @Override
    public void sayHello(EchoGreeterProto.EchoRequest request,
                         StreamObserver<EchoGreeterProto.EchoReply> responseObserver) {
        //ThreadUtil.sleep(600, TimeUnit.SECONDS);
        ThreadUtil.sleep(600, TimeUnit.SECONDS);
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
