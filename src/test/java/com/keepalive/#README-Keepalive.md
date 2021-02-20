## grpc keepalive

## 总结：grpc与时间有关的配置
> PS:{@link com.keepalive.ClientPingFrameTest}
- 后端服务
    - .permitKeepAliveTime(10, TimeUnit.SECONDS)//后端规定的速率,在规定的速率内可以发送2次ping，如果设置为10秒则不会出现触发too_many_pings 
        ```
      ps:虽然permitKeepAliveTime(10, TimeUnit.SECONDS)可以避免触发too_many_pings问题，但是它也隐藏了用户一些不合理的设置，占用过多服务器资源与带宽。
      （但目前个人认为此理由在大多数场景下，都比较牵强，暂不推荐byArvin）
      个人建议可以把后端服务设置为permitKeepAliveTime(10, TimeUnit.SECONDS)
      ```
- 客户端
    - .keepAliveTime(10, TimeUnit.SECONDS) //此时使用用户自定义时间10秒（PS:默认值也是10秒）（channel连接级别，此级别主要是判断连接是否处于假死状态）
        - .keepAliveWithoutCalls(true)
        - .keepAliveTime(10, TimeUnit.SECONDS) //此时使用用户自定义时间10秒（PS:默认值也是10秒）
        - .keepAliveTimeout(10, TimeUnit.NANOSECONDS)
    - futureStub.sayHello(echoRequest).get(5, TimeUnit.SECONDS);// 5秒超时 （方法级别）
    - EchoGreeterGrpc.newFutureStub(channel).withDeadlineAfter(20, TimeUnit.SECONDS);// 20秒超时 （stub级别|通道级别）
- 错误异常信息
    - UNAVAILABLE: Keepalive failed. The connection is likely gone （PS:后端服务无法响应客户端的ping请求时）
    - RESOURCE_EXHAUSTED: Bandwidth exhausted （PS:触发too_many_pings，3次ping请求）
    - TimeoutException: Waited 40 seconds for io.grpc.stub.ClientCalls（PS:触发futureStub.sayHello(echoRequest).get(5, TimeUnit.SECONDS)）
    - DEADLINE_EXCEEDED: deadline exceeded after 19890781200ns（PS:触发EchoGreeterGrpc.newFutureStub(channel).withDeadlineAfter(20, TimeUnit.SECONDS)）
- 合理的时间配置参考方案
    ```
    方法级别 < stub级别|通道级别 < channel连接级别 <
    示例：（1分钟超时）
    客户端设置：
    方法级别 （超时时间：60秒） < stub级别|通道级别 （超时时间：70秒）< channel连接级别（超时时间：90秒）
    后端服务设置：
    permitKeepAliveTime(10, TimeUnit.SECONDS)（PS:避免触发too_many_pings问题）
    说明：
    此配置可以真实的反应后端服务的处理情况，同时间也减少了PING请求次数，节约后端服务资源
    ```

## 核心类
- io.grpc.internal.KeepAliveManager
```
1.
sendPing
2.
shutdown
```

## 解决too_many_pings:Sent GOAWAY 
- server 
```
Server server = NettyServerBuilder.forPort(port)
                /**
                 * too_many_pings:Sent GOAWAY
                 *
                 */
                .permitKeepAliveWithoutCalls(true)
                .permitKeepAliveTime(1, TimeUnit.SECONDS)
                .addService(new EchoGreeterServiceImpl())
                .build();
```
- client 
```
  channel = ManagedChannelBuilder
                .forAddress("127.0.0.1", port)
                /**
                 * 设置当连接上没有未完成的RPC时是否执行keepalive。
                 * *默认为{@code false}。
                 */
                .keepAliveWithoutCalls(true)
                /**
                 *
                 */
                .keepAliveTime(1, TimeUnit.SECONDS)
                .usePlaintext()
                .build();
```
- 总结
```
permitKeepAliveTime(10, TimeUnit.SECONDS) 时间应该小于keepAliveTime(1, TimeUnit.SECONDS)的时间即可解决too_many_pings的问题
permitKeepAliveTime为10秒时，则不会触发too_many_pings的问题，因为10秒已经是keepAliveTime的最小值了。
PS:
permitKeepAliveWithoutCalls的配置主要是为了触发too_many_pings的，从而减少带宽与cpu

```

## 问题：io.grpc.StatusRuntimeException: UNAVAILABLE:Keepalive failed. The connection is likely gone
- server 
```
 @Test
    @SneakyThrows
    public void keepaliveTest() {
        Server server = NettyServerBuilder.forPort(port)
                .intercept(new ServerTracingLogInterceptor())
                //只用一个线程：然后在当前线程产生阻塞，模拟服务处理缓慢
                .directExecutor()
                /**
                 * too_many_pings:Sent GOAWAY
                 * 在规定的时间内发送了超过2次ping请求
                 */
                .permitKeepAliveWithoutCalls(true)
                /**
                 * DEFAULT_SERVER_KEEPALIVE_TIMEOUT_NANOS = TimeUnit.SECONDS.toNanos(20L);
                 * 默认值为20秒
                 */
                .permitKeepAliveTime(10, TimeUnit.SECONDS)
                //keepAliveTime与keepAliveTimeout两个配置不建议在服务器端使用
                //.keepAliveTime(1, TimeUnit.SECONDS)
                //.keepAliveTimeout(1, TimeUnit.NANOSECONDS)
                .addService(new EchoGreeterServiceImpl())
                .build();
        server.start();
        log.info("Start server port[{}]", port);
        server.awaitTermination();
    }
--------------------------------------------------------------------------------------------------------
  EchoGreeterServiceImpl
    @Override
    public void sayHello(EchoGreeterProto.EchoRequest request,
                         StreamObserver<EchoGreeterProto.EchoReply> responseObserver) {
        //模拟业务处理时间长，线程被占用。
        //ThreadUtil.sleep(600, TimeUnit.SECONDS);
        ThreadUtil.sleep(600, TimeUnit.SECONDS);
        responseObserver.onNext(newReplyData(request.getSize()));
        responseObserver.onCompleted();
    }
```
- client 
```
private ManagedChannel channel;

       @Before
        public void init() {
            channel = ManagedChannelBuilder
                    .forAddress("127.0.0.1", port)
                    /**
                     * 代表：空闲模式也要触发ping请求
                     * 设置当连接上没有未完成的RPC时是否执行keepalive。
                     * *默认为{@code false}。
                     */
                    //.keepAliveWithoutCalls(true)
                    /**
                     * 空闲模式与非空闲模式（有请求正在处理）：触发
                     * 如果长时间没有收到读时，则发送ping来判断当前连接是否存活。
                     * 默认为20秒，最小为10秒
                     * 如果keepAliveTimeout(1, TimeUnit.SECONDS)则设置为最小值10秒，
                     * 如果客户端频发发送ping请求，则出发too_many_pings
                     */
                    .keepAliveTime(5, TimeUnit.SECONDS)
                    /**
                     * keepAliveTimeout设置决定触发pingTimeout方法（PS:）的速度
                     * 默认为：10秒,最小值为10毫秒 
                     */
                    .keepAliveTimeout(10, TimeUnit.SECONDS)
                    .usePlaintext()
                    .build();
        }
/**
     * 问题：io.grpc.StatusRuntimeException: UNAVAILABLE:Keepalive failed. The connection is likely gone
     */
    @Test
    public void keepaliveForKeepaliveFailedTest() {
        EchoGreeterGrpc.EchoGreeterBlockingStub blockingStub = EchoGreeterGrpc.newBlockingStub(channel);
        EchoGreeterProto.EchoRequest echoRequest = EchoGreeterProto.EchoRequest.newBuilder()
                .setSize(10)
                .build();
        EchoGreeterProto.EchoReply echoReply = null;
        try {
            //模拟同时有多个线程在请求服务端
            ThreadUtil.newSingleExecutor().execute(this::AsyncCallWithSleep);
            echoReply = blockingStub.sayHello(echoRequest);
            log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
            ThreadUtil.sleep(60, TimeUnit.SECONDS);
            echoReply = blockingStub.sayHello(echoRequest);
            log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
            ThreadUtil.sleep(60, TimeUnit.SECONDS);
            echoReply = blockingStub.sayHello(echoRequest);
            log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
        } catch (Exception ex) {
            log.error("[T1]Occur Exception!", ex);
        }
        try {
            echoReply = blockingStub.sayHello(echoRequest);
            log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
            ThreadUtil.sleep(60, TimeUnit.SECONDS);
            echoReply = blockingStub.sayHello(echoRequest);
            log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
            ThreadUtil.sleep(60, TimeUnit.SECONDS);
            echoReply = blockingStub.sayHello(echoRequest);
            log.info("At time:[{}],message:[{}].", echoReply.getAtTime(), echoReply.getMessage());
        } catch (Exception ex) {
            log.error("[T2]Occur Exception!", ex);
        }
    }
```

- 总结
```
触发Keepalive failed. The connection is likely gone的根本原因是因为：后端服务无法响应客户端的ping请求
PS:
后端服务无法响应客户端的ping请求有两种情况：
1.
客户端与后端服务之间的网络不通过。连接超时时间过长造成。
方法检测的PING操作产生的UNAVAILABLE:Keepalive failed. The connection is likely gone异常，在UNAVAILABLE:io exception|UNAVAILABLE:connection timeout之间触发。
可以通过配置：
NettyChannelBuilder
 .forTarget(target)
 .withOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10) //设置连接超时时间
2.
服务端处理能力不足，发生阻塞,所有线程都被占用，无法响应客户端发来过来的ping请求。触发pingTimeout方法。
ping超时后会报Keepalive failed. The connection is likely gone
```