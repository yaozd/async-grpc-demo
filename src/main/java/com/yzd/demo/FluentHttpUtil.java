package com.yzd.demo;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Slf4j
public class FluentHttpUtil {

    private static final String API_ROUTER_URL = "http://localhost:9312/applyConfig";
    /**
     * 指建立连接的超时时间
     */
    public static final int REQUEST_CONNECT_TIMEOUT = 3000;
    public static final int REQUEST_SOCKET_TIMEOUT = 10 * 1000;

    public static void sendConfig(String path) {
        try {
            String configData = readResource(path);
            String response = Request.Post(API_ROUTER_URL)
                    //短链接模式 close http
                    .setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE)
                    .bodyString(configData, ContentType.APPLICATION_JSON)
                    .connectTimeout(REQUEST_CONNECT_TIMEOUT)
                    .socketTimeout(REQUEST_SOCKET_TIMEOUT)
                    .execute().handleResponse((ResponseHandler<String>) httpResponse ->
                            EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8));
            log.info("response: {}", response);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String readResource(String path) throws IOException {
        return readResource(path, Collectors.joining());
    }

    public static String readResource(String path, Collector<CharSequence, ?, String> collector) throws IOException {
        try (InputStream fileIs = FluentHttpUtil.class.getResourceAsStream(path);
             InputStreamReader fileIsr = new InputStreamReader(fileIs);
             BufferedReader fileReader = new BufferedReader(fileIsr)) {
            return fileReader.lines().collect(collector);
        }
    }
}
