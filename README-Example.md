## Example
- [GRPC的四种服务类型](https://www.cnblogs.com/resentment/p/6792029.html)
    ```
  package com.examples
  ```
- [GRPC错误处理](https://www.cnblogs.com/resentment/p/6883153.html)
- [GRPC拦截器](https://www.cnblogs.com/resentment/p/6818753.html)
    ```
  serviceDef = ServerInterceptors.intercept(serviceDef,
      interceptor1,
      interceptor2,
      interceptor3);
  
  // is equivalent to
  
  serviceDef = ServerInterceptors.intercept(serviceDef, interceptor1);
  serviceDef = ServerInterceptors.intercept(serviceDef, interceptor2);
  serviceDef = ServerInterceptors.intercept(serviceDef, interceptor3);
  ```
- [GRPC测试](https://www.cnblogs.com/resentment/p/6914283.html)
- [GRPC与JSON格式互转](https://www.cnblogs.com/resentment/p/6938180.html)
    ```
  grpc转json
  @Test
  public void printer() throws InvalidProtocolBufferException {
  	//grpc实例
      ProtoObj.Person person = ProtoObj.Person.newBuilder().setMyName("World").build();
      System.out.println( JsonFormat.printer().print(person));
  }
  json转grpc
  @Test
  public void parse() throws InvalidProtocolBufferException {
  	//创建builder
      ProtoObj.Person.Builder builder=ProtoObj.Person.newBuilder();
  	//使用parser进行merge，这里是merge只是覆盖重复的字段
      JsonFormat.parser().merge("{myName:'aaa'}",builder);
      System.out.println(builder.build().getMyName());
  }
  ```
- []()