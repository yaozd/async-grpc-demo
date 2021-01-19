package com.opentracing;

import com.mattie.grpc.GreeterGrpc;
import com.mattie.grpc.HelloWorldProtos;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class TracedService {
	private int port = 50051;
	private Server server;
	
	void start() throws IOException, InterruptedException {
		server = ServerBuilder.forPort(port)
				.addService(new GreeterImpl())
				.build()
				.start();
		log.info("Start server port[{}]",port);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				TracedService.this.stop();
			}
		});
	}
	
	void startWithInterceptor(ServerTracingInterceptor tracingInterceptor) throws IOException {
				
		server = ServerBuilder.forPort(port)
				.addService(tracingInterceptor.intercept(new GreeterImpl()))
				.build()
				.start();
		log.info("Start server with tracing interceptor, port[{}]",port);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				TracedService.this.stop();
			}
		});
	}
	
	void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}
	
	void stop() {
		if (server != null) {
			server.shutdown();
		}
	}
	
	private class GreeterImpl extends GreeterGrpc.GreeterImplBase {
		@Override
		public void sayHello(HelloWorldProtos.HelloRequest req, StreamObserver<HelloWorldProtos.HelloReply> responseObserver) {
			HelloWorldProtos.HelloReply reply = HelloWorldProtos.HelloReply.newBuilder().setMessage("Hello").build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();
		}
	}
}
