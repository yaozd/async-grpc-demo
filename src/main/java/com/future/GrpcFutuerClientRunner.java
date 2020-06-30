package com.future;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * java '-Dapp.log.level=error' '-Dapp.target=localhost:8888' '-Dapp.forks=1' '-Dapp.threads=1' '-Dapp.iterations=1'   -jar .\grpc-test-demo-fd9bb8c.jar
 *
 * @Author: yaozh
 * @Description:
 */
public class GrpcFutuerClientRunner {
    private static final int threads = Integer.parseInt(System.getProperty("app.threads", "1"));
    private static final int forks = Integer.parseInt(System.getProperty("app.forks", "1"));

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(GrpcFutuerClient.class.getName())
                //方法仅运行一次(用于冷测试模式)
                .mode(Mode.SingleShotTime)
                //线程个数
                .threads(threads)
                //实际每次迭代次数
                .measurementIterations(1)
                //forks:进程
                .forks(forks)
                .build();

        new org.openjdk.jmh.runner.Runner(opt).run();
    }
}
