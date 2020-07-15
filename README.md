# 启动参考
```
WIN:
java '-Dapp.log.level=error' -jar .\stream-server-f06a3c3.jar
```

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

## 参考：
- [Maven: jar包名自动添加git commit id](https://blog.csdn.net/wuzhimang/article/details/79393815)
- [maven: 给jar包指定 定制的 logback.xml](https://blog.csdn.net/bigtree_3721/article/details/81289144)
- [对Guava类库的注释类型 VisibleForTesting的理解](https://www.cnblogs.com/yanlongpankow/p/6240563.html)
```
有一次问同事：除了用JAVA Reflection 来测试私有方法外，还有什么好的方法可以更简单的测试私有方法。
同事回答，可以用Guava的VisibleForTesting。于是看了看这个注释的用法。
VisibleForTesting的注解来提醒其他程序员: 这里为了测试私有方法把私有方法改成了Protected(受保护的)并放宽了访问限制
```
