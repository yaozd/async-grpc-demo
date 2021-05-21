## grpc-常见问题

## grpc backoff机制 指数退避算法exponential back-off algorithm
- [指数退避算法exponential back-off algorithm](https://blog.csdn.net/yuanyl/article/details/45150539)
```
1.
## 作用
grpc back-off主要是在轮训情况下减少连接消耗，也减少连接端口的占用
2.
## 影响监听模式，stream watch情况下：
back-off 会增加再次连接的时长
3.
## 解决方案
//重置backoff
grpcChannel.resetConnectBackoff();
//
private void watchData() {
     grpcChannel.resetConnectBackoff();
     zoneClusterStub.watchZoneCluster(
             Cluster.WatchZoneClusterReq.newBuilder().setRev(revision).build(),
             new WatchObserver(this));
 }

```