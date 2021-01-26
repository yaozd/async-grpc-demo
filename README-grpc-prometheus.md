## prometheus/client_java -官网推荐参考
- [https://github.com/prometheus/client_java](https://github.com/prometheus/client_java) -推荐参考byArvin
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

## 指标输出
- [代码示例](https://www.programcreek.com/java-api-examples/?class=io.prometheus.client.CollectorRegistry&method=register)
- 指标转为字符串
    ```
  CollectorRegistry collector = new CollectorRegistry();
    collector.register(new RatisDropwizardExports(dropWizardMetricRegistry));
  
    //export metrics to the string
    StringWriter writer = new StringWriter();
    TextFormat.write004(writer, collector.metricFamilySamples());
  
    System.out.println(writer.toString());
  ```
- 指标通过HTTP暴露出去
    ```
  // Metrics
    ServletHolder codahaleMetricsServlet = new ServletHolder("default",
        new com.codahale.metrics.servlets.MetricsServlet(METRICS));
    context.addServlet(codahaleMetricsServlet, codahaleMetricsEndpoint);
  
    // Prometheus
    CollectorRegistry collectorRegistry = new CollectorRegistry();
    collectorRegistry.register(new DropwizardExports(METRICS));
    ServletHolder prometheusServlet = new ServletHolder("prometheus",
        new io.prometheus.client.exporter.MetricsServlet(collectorRegistry));
    context.addServlet(prometheusServlet, prometheusEndpoint);
  ```
- 