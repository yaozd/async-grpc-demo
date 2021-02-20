# grpc 服务发现
## name resolver
```
nameResolverFactory(NameResolver.Factory resolverFactory)，指定使用的NameResolver
name resolver接收一个Uri，并返回多个地址。
如果未调用此方法，将通过java的service provider机制发现实现了NameResoverProvider机制的类。因此，若没有自定义name resolver，默认将使用grpc提供的DnsNameResolverProvider。
```

## 参考
- etcd-resolver项目中的EtcdNameResolverProvider 
- [gRPC客户端详解](https://www.jianshu.com/p/866ca99abc0e?utm_campaign=hugo)
- []()