package com.examples;

import lombok.SneakyThrows;
import org.junit.Test;

/**
 * @Author: yaozh
 * @Description:
 */
public class FooServerTest {

    @Test
    @SneakyThrows
    public void start() {
        FooServer fooServer = new FooServer();
        fooServer.start();
        fooServer.blockUntilShutdown();
    }

    @Test
    @SneakyThrows
    public void startMockError() {
        FooServer fooServer = new FooServer();
        fooServer.startMockError();
        fooServer.blockUntilShutdown();
    }
}