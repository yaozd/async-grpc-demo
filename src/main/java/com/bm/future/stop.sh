#!/bin/sh 
kill -9 `jps -l|grep com.bm.future.GrpcFutuerClientRunner|awk '{print $1}'`   
kill -9 `jps -l|grep runner.ForkedMain|awk '{print $1}'` 
