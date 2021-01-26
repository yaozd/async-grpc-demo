# 启动参考
```
WIN:
java '-Dapp.log.level=error' -jar .\stream-server-f06a3c3.jar
```
# Grpc入门
- [RPC原理以及GRPC详解](https://www.cnblogs.com/awesomeHai/p/liuhai.html)
- [grpc应用详解](https://www.jianshu.com/p/5e75b20a267f)
- [gRPC](https://blog.csdn.net/xuduorui/article/details/78278808)
- [GRPC的四种服务类型](https://www.cnblogs.com/resentment/p/6792029.html)
-

# Grpc几种通信方式
```
Grpc几种通信方式:

gRPC主要有4种请求／响应模式，分别是：

(1) 简单模式（Simple RPC）

这种模式最为传统，即客户端发起一次请求，服务端响应一个数据，
这和大家平时熟悉的RPC没有什么大的区别，所以不再详细介绍。

(2) 服务端数据流模式（Server-side streaming RPC）

这种模式是客户端发起一次请求，服务端返回一段连续的数据流。
典型的例子是客户端向服务端发送一个股票代码，服务端就把该股票的实时数据源源不断的返回给客户端。

(3) 客户端数据流模式（Client-side streaming RPC）

与服务端数据流模式相反，这次是客户端源源不断的向服务端发送数据流，
而在发送结束后，由服务端返回一个响应。典型的例子是物联网终端向服务器报送数据。

(4) 双向数据流模式（Bidirectional streaming RPC）

顾名思义，这是客户端和服务端都可以向对方发送数据流，
这个时候双方的数据可以同时互相发送，也就是可以实现实时交互。典型的例子是聊天机器人

```

# async-grpc-demo

## 请求异步处理：ListenableFuture和CompletableFuture简单小结 
- [ListenableFuture和CompletableFuture简单小结](https://blog.csdn.net/Androidlushangderen/article/details/80372711)

## 双向流模式
- [gRPC入门-双向流式通信](https://www.jianshu.com/p/323806eb91bb)

## grpc-服务发现
- [java grpc client集成consul服务发现](https://www.jianshu.com/p/997505834bf5)

## running the application

This is a simple application demonstrating a Client-Server Application exchanging messages asynchronously using gRPC.

To run, execute the following in a terminal window

```shell
mvn package exec:java -Dexec.mainClass=org.nuhara.demos.GrpcServer
```

then, run the following in a different window

```shell
mvn exec:java -Dexec.mainClass=org.nuhara.demos.GrpcClient
```

## OpenTracing

This demo app makes use of Jaeger as an OpenTracing implementation.  To monitor the trace logs, you can use the all-in-one Jaeger container.

```shell
$ docker pull jaegertracing/all-in-one
$ docker run -d --name jaeger \
  -e COLLECTOR_ZIPKIN_HTTP_PORT=9411 \
  -p 5775:5775/udp \
  -p 6831:6831/udp \
  -p 6832:6832/udp \
  -p 5778:5778 \
  -p 16686:16686 \
  -p 14268:14268 \
  -p 9411:9411 \
  jaegertracing/all-in-one:latest
```
Navigate to ```https://www.jaegertracing.io/docs/getting-started/``` to access the Jaeger UI.

## prometheus/client_java -官网推荐参考
- [https://github.com/prometheus/client_java](https://github.com/prometheus/client_java)
- [prometheus-demo](https://github.com/yuriiknowsjava/prometheus-demo)

## grpc-prometheus
- [java-grpc-prometheus](https://github.com/lchenn/java-grpc-prometheus) -推荐参考byArvin
- [java-grpc-prometheus](https://gitee.com/mirrors_grpc-ecosystem/java-grpc-prometheus) -代码无法运行
- [java-grpc-prometheus:custom-buckets](https://gitee.com/mirrors_grpc-ecosystem/java-grpc-prometheus/tree/custom-buckets)-代码无法运行
- [prometheus-demo](https://github.com/yuriiknowsjava/prometheus-demo)
- 涉及jar包
    ```
   <dependency>
     <groupId>io.prometheus</groupId>
     <artifactId>simpleclient</artifactId>
     <version>0.9.0</version>
   </dependency>
   <dependency>
     <groupId>io.prometheus</groupId>
     <artifactId>simpleclient_hotspot</artifactId>
     <version>0.9.0</version>
   </dependency>
  <!-- Exposition HTTPServer-->
   <dependency>
   	<groupId>io.prometheus</groupId>
   	<artifactId>simpleclient_httpserver</artifactId>
   	<version>0.9.0</version>
   </dependency>
  ```

## 通过阿里云Prometheus监控JVM
- [通过阿里云Prometheus监控JVM](https://help.aliyun.com/document_detail/139661.html)


## 参考：
- [Maven: jar包名自动添加git commit id](https://blog.csdn.net/wuzhimang/article/details/79393815)
- [maven: 给jar包指定 定制的 logback.xml](https://blog.csdn.net/bigtree_3721/article/details/81289144)
- [对Guava类库的注释类型 VisibleForTesting的理解](https://www.cnblogs.com/yanlongpankow/p/6240563.html)
```
有一次问同事：除了用JAVA Reflection 来测试私有方法外，还有什么好的方法可以更简单的测试私有方法。
同事回答，可以用Guava的VisibleForTesting。于是看了看这个注释的用法。
VisibleForTesting的注解来提醒其他程序员: 这里为了测试私有方法把私有方法改成了Protected(受保护的)并放宽了访问限制
```
## 启动 -cp
```
java -cp .\grpc-test-demo-5866ea2.jar com.mattie.demo.StreamServer
//No log runner
java '-Dapp.log.level=error' -cp .\grpc-test-demo-5866ea2.jar com.mattie.demo.StreamServer
```

## [proto数据结构定义](https://github.com/protocolbuffers/protobuf/blob/master/src/google/protobuf)
- [https://github.com/protocolbuffers/protobuf/blob/master/src/google/protobuf](https://github.com/protocolbuffers/protobuf/blob/master/src/google/protobuf)
```
wrapper.proto 包装后的基本数据类型
empty.proto void方法的返回类型
map_untittesst.proto map数据结构类型参考

```
- [protobuf 三个关键字required、optional、repeated的理解](https://lovemiffy.blog.csdn.net/article/details/82751720)
```
required关键字
顾名思义，就是必须的意思，数据发送方和接收方都必须处理这个字段，不然还怎么通讯呢

optional关键字
字面意思是可选的意思，具体protobuf里面怎么处理这个字段呢，就是protobuf处理的时候另外加了一个bool的变量，用来标记这个optional字段是否有值，发送方在发送的时候，如果这个字段有值，那么就给bool变量标记为true，否则就标记为false，接收方在收到这个字段的同时，也会收到发送方同时发送的bool变量，拿着bool变量就知道这个字段是否有值了，这就是option的意思。

这也就是他们说的所谓平滑升级，无非就是个兼容的意思。

其实和传输参数的时候，给出数组地址和数组数量是一个道理。

repeated关键字
字面意思大概是重复的意思，其实protobuf处理这个字段的时候，也是optional字段一样，另外加了一个count计数变量，用于标明这个字段有多少个，这样发送方发送的时候，同时发送了count计数变量和这个字段的起始地址，接收方在接受到数据之后，按照count来解析对应的数据即可。
```
- [Oneof](https://www.cnblogs.com/sanshengshui/p/9739521.html)
```
最多只能同时设置一个字段:
如果您有一个包含许多字段的消息，并且最多只能同时设置一个字段，则可以使用oneof功能强制执行此行为并节省内存。
```
- [Protobuf 语言指南(proto3)](https://www.cnblogs.com/sanshengshui/p/9739521.html)

## 日志的链路追踪
- http
    - [SpringBoot日志的链路追踪](https://blog.lupf.cn/articles/2020/06/04/1591273110824.html)
- grpc