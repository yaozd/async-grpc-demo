## grpc keepalive

> keepalive 主要触发ping操作是为判断当前连接是否处于假死状态，因此不要滥用。

## 为什么要设置Keepalive
   - [golang grpc keepalive](https://blog.csdn.net/hatlonely/article/details/103282712/)
        ```
      grpc 客户端报错 rpc error: code = Unavailable desc = transport is closing，
     原因是连接长时间没有使用，被服务端断开，这种情况通过简单粗暴的重试策略可以解决，更加优雅的解决方案是增加保持连接策略
     ```
## 如果Keepalive配置不合理，会有哪些影响
   - keepalive配置时间过小，在后端服务非阻塞情况下，会触发too_many_pings问题
        ```
     too_many_pings问题
     RESOURCE_EXHAUSTED: Bandwidth exhausted
     客户端发送的ping存在太多不符合规则的，则服务器发送ENHANCE_YOUR_CALM的GOAWAY帧
     ```
   - keepalive配置时间过小，在后端服务阻塞情况下
        ```
     解决keepalive时间配置不合理，造成UNAVAILABLE: Keepalive failed. The connection is likely gone异常提前触发，
     覆盖程序实际错误异常，如：
     1.TimeoutException: Waited 40 seconds for io.grpc.stub.ClientCalls
     2.DEADLINE_EXCEEDED: deadline exceeded after 19890781200ns
     3.UNAVAILABLE:io exception
     ```

## keepalive 相关默认值
   - [grpc keepalive使用指南](https://blog.csdn.net/zhaominpro/article/details/103127023)
   
        |  channelArgument   | Client  | Server  |
        |  ----  | ----  | ----  |
        | GRPC_ARG_KEEPALIVE_TIME_MS  | INT_MAX(disabled) | 7200000(2 hours)|
        | GRPC_ARG_KEEPALIVE_TIMEOUT_MS  | 20000(20 seconds) | 20000(20 seconds) |
        | GRPC_ARG_KEEPALIVE_PERMIT_WITHOUT_CALLS  | 0(false)  | 0(false) |
        | GRPC_ARG_HTTP2_MAX_PINGS_WITHOUT_DATA  | 2 | 2|
        | GRPC_ARG_HTTP2_MIN_SENT_PING_INTERVAL_WITHOUT_DATA_MS  |300000(5 minutes)  | 300000(5 minutes)|
        | GRPC_ARG_HTTP2_MIN_RECV_PING_INTERVAL_WITHOUT_DATA_MS  | N/A | 300000(5 minutes)|
        | GRPC_ARG_HTTP2_MAX_PING_STRIKES  | N/A | 2 |

## grpc keepalive使用指南
   - [grpc keepalive使用指南](https://blog.csdn.net/zhaominpro/article/details/103127023) 此版本的GRPC_ARG_XXX参数，应该是非java版的。只是对里面的解读做参考 byArvin
   - Keepalive计时器何时启动？
        ```
        创建完transport后,不管连接是否创建成功，都将启动keepalive计时器（定时任务）。keepalive发送ping请求的时间间隔为：keepAliveTime(10, TimeUnit.SECONDS)
        
        PS:如果连接在创建过程中，到达keepalive时也会发送ping请求，ping请求最小间隔时间为10秒，如果连接创建过程超过10秒，客户端会发送ping请求，此时会触发：
        UNAVAILABLE: Keepalive failed. The connection is likely gone 异常
        ```
   - 当keepalive计时器触发时会发生什么？
        ```
        当keepalive计时器触发时，gRPC Core将尝试在传输中发送keepalive ping。但是以下情况可以阻止此ping的发送
        该transport上没有活动调用，并且GRPC_ARG_KEEPALIVE_PERMIT_WITHOUT_CALLS为false。
        transport中已发送的ping数目（在transport中没有其他data发送时）已超过GRPC_ARG_HTTP2_MAX_PINGS_WITHOUT_DATA。
        自上次ping以来经过的时间少于GRPC_ARG_HTTP2_MIN_SENT_PING_INTERVAL_WITHOUT_DATA_MS。
        如果keepalive的ping没有被blocked并在transport中发送，那么将启动keepalive watchdog计时器：如果在触发（timeout）前还未收到ping的确认，就会关闭transport
        ```
   - 为什么我收到错误代码为ENHANCE_YOUR_CALM的GOAWAY？RESOURCE_EXHAUSTED: Bandwidth exhausted,HTTP/2 error code: ENHANCE_YOUR_CALM
        ```
        简单讲就是发送ping请求次数太频繁
        RESOURCE_EXHAUSTED: Bandwidth exhausted （PS:触发too_many_pings，3次ping请求）
        java 版本的MAX_PING_STRIKES = 2;（单位时间内可以发送两次）
        如果客户端发送的ping存在太多不符合规则的，则服务器发送ENHANCE_YOUR_CALM的GOAWAY帧。例如
        如果服务器将GRPC_ARG_KEEPALIVE_PERMIT_WITHOUT_CALLS设置为false，但客户端却在没有任何请求的transport中发送ping。
        如果客户端设置的GRPC_ARG_HTTP2_MIN_SENT_PING_INTERVAL_WITHOUT_DATA_MS的值低于服务器的GRPC_ARG_HTTP2_MIN_RECV_PING_INTERVAL_WITHOUT_DATA_MS的值。
        PS:
        GRPC_ARG_HTTP2_MAX_PING_STRIKES
        此arg控制在发送HTTP2 GOAWAY帧并关闭传输之前，服务器允许的错误ping的最大数量。将其设置为0允许服务器接受任意数量的错误ping。
        (注：也就是达到这个数量的ping strike就会发送GOWAY帧–用于发起关闭连接的请求，
        或者警示严重错误。GOAWAY 会停止接收新流，并且关闭连接前会处理完先前建立的流)
    ```


## 总结：grpc与时间有关的配置
> PS:{@link com.keepalive.ClientPingFrameTest}
- 后端服务
    - .permitKeepAliveTime(10, TimeUnit.SECONDS)//后端规定的速率,在规定的速率内可以发送2次ping，如果设置为10秒则不会出现触发too_many_pings 
        ```
      ps:虽然permitKeepAliveTime(10, TimeUnit.SECONDS)可以避免触发too_many_pings问题，但是它也隐藏了用户一些不合理的设置，占用过多服务器资源与带宽。
      （但目前个人认为此理由在大多数场景下，都比较牵强，暂不推荐byArvin）
      个人建议可以把后端服务设置为permitKeepAliveTime(10, TimeUnit.SECONDS)
      此配置的缺点：导致错误提示不正确，会误导问题解决的思路
      在后端服务不可用时，10秒钟会在deadline之前触发关闭。
      此时异常信息：UNAVAILABLE: Keepalive failed. The connection is likely gone （PS:后端服务无法响应客户端的ping请求时）
      但实际上是后端服务发生的阻塞。应该触发TimeoutException异常或DEADLINE_EXCEEDED异常更为合适。
      PS:
      此时的后端服务不可用时，存在多种情况
      情况1：当时有大量请求，导致所有后端服务不可用
      情况2：客户端负载均衡配置不正确，选择的是PickFirstLoadBalancerProvider，所有流量都打到同一个后端服务节点上，也会导致此节点服务不可用 
      ```
- 客户端
    - NettyChannelBuilder.forTarget(target).withOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10) //设置连接超时时间
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
  方法级别 （futureStub.sayHello(echoRequest).get超时时间：60秒） < stub级别|通道级别 （newFutureStub(channel).withDeadlineAfter超时时间：70秒）< channel连接级别（NettyChannelBuilder.keepAliveTime超时时间：120秒）
    方法级别 （futureStub.sayHello(echoRequest).get超时时间：60秒） < stub级别|通道级别 （newFutureStub(channel).withDeadlineAfter超时时间：90秒）< channel连接级别（NettyChannelBuilder.keepAliveTime超时时间：120秒）
    后端服务设置：
    permitKeepAliveTime(10, TimeUnit.SECONDS)（PS:避免触发too_many_pings问题）
    说明：
    此配置可以真实的反应后端服务的处理情况，同时间也减少了PING请求次数，节约后端服务资源
    但是在极端情况下，如果后端服务无法响应ping请求时，一样会触发UNAVAILABLE: Keepalive failed. The connection is likely gone
    ```
- permitKeepAliveTime与keepAliveTime之间的关系
    ```
  keepAliveTime>permitKeepAliveTime/2
  eg:
  permitKeepAliveTime=120s
  keepAliveTime最小值为61s(120/2=60)
  keepAliveTime=61s>permitKeepAliveTime=120/2=60
  参考示值：
  理论：
  keepaliveTime=70|120,permitKeepAliveTime=120s
  实际：
  keepaliveTime=150s>permitKeepAliveTime=120s
  注：grpc的版本不同触发逻辑也不同，最好keepaliveTime值要大于permitKeepAliveTime
  PS:
  PING_STRIKES=2
  失败后会触发AtomBackOff ,keepaliveTime会扩大1倍（61*2=122）
  io.grpc.internal.AtomicBackoff$State backoff
  警告: Increased keepalive time nanos to 40,000,000,000
  注：grpc的版本不同触发逻辑也不同，最好keepaliveTime值要大于permitKeepAliveTime
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
客户端与后端服务之间的流量不均衡。如果把所有流量都打到同一个后端服务节点，也会导致此节点服务不可用。
3.
服务端处理能力不足，发生阻塞,所有线程都被占用，无法响应客户端发来过来的ping请求。触发pingTimeout方法。
ping超时后会报Keepalive failed. The connection is likely gone
```