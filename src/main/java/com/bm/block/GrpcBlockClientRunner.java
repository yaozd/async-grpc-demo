package com.bm.block;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

/**
 * java '-Dapp.log.level=error' '-Dapp.target=localhost:8888' '-Dapp.forks=1' '-Dapp.threads=1' '-Dapp.measurementTime=60' '-Dapp.measurementIterations=2'   -jar .\grpc-test-demo-fd9bb8c.jar
 *
 * @Author: yaozh
 * @Description:
 */
public class GrpcBlockClientRunner {
    private static final int threads = Integer.parseInt(System.getProperty("app.threads", "200"));
    private static final int forks = Integer.parseInt(System.getProperty("app.forks", "100"));
    //seconds
    private static final int measurementTime = Integer.parseInt(System.getProperty("app.measurementTime", "60"));
    private static final int measurementIterations = Integer.parseInt(System.getProperty("app.measurementIterations", "2"));
    public static final String target = System.getProperty("app.target", "localhost:8888");

    /**
     * 测试场景
     * Connections exceed max node size 300, target node(testDemobackend:127.0.0.1:8899:-:RAW)
     *
     * @param args
     * @throws RunnerException
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(GrpcBlockClient.class.getName())
                .mode(Mode.Throughput)
                //线程个数
                .threads(threads)
                //预热次数
                .warmupIterations(1)
                //预热时间
                .warmupTime(TimeValue.seconds(5))
                //度量
                .measurementTime(TimeValue.seconds(measurementTime))
                //实际每次迭代次数
                .measurementIterations(measurementIterations)
                //forks:进程
                .forks(forks)
                .build();

        new org.openjdk.jmh.runner.Runner(opt).run();
    }
}
