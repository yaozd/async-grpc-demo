package com.opentracing;

import com.mattie.grpc.GreeterGrpc;
import com.mattie.grpc.HelloWorldProtos;
import com.mattie.grpc.HelloWorldProtos.HelloRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TracedClient {
	private final ManagedChannel channel;
	private final GreeterGrpc.GreeterBlockingStub blockingStub;
	
	public TracedClient(String host, int port, ClientTracingInterceptor tracingInterceptor) {
		channel = ManagedChannelBuilder.forAddress(host, port)
				.usePlaintext(true)
				.build();
		
		if(tracingInterceptor==null) {
			blockingStub = GreeterGrpc.newBlockingStub(channel);
		} else {
			blockingStub = GreeterGrpc.newBlockingStub(tracingInterceptor.intercept(channel));
		}		
	}
	
	void shutdown() throws InterruptedException {
		channel.shutdown();
	}
	
	boolean greet(String name) {
		HelloRequest request = HelloRequest.newBuilder().setMessage(name).build();
		try {
			HelloWorldProtos.HelloReply helloReply = blockingStub.sayHello(request);
			log.info("Client receive message [{}]",helloReply.getMessage());
		} catch (Exception e) {
			return false;
		} finally {
			try { this.shutdown(); }
			catch (Exception e) { return false; }
		}
		return true;
		
	}
}
