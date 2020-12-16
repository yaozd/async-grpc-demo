#!/bin/sh
while :
do
    echo "Runner [ com.bm.future.GrpcFutuerClientRunner ]!"
    nohup java -cp grpc-test-demo.jar com.bm.future.GrpcFutuerClientRunner  >/dev/null 2>&1 &
    sleep 2
    jps -l
    sleep 60
    kill -9 `jps -l|grep com.bm.future.GrpcFutuerClientRunner|awk '{print $1}'`   
    kill -9 `jps -l|grep runner.ForkedMain|awk '{print $1}'` 
done
