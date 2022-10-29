# ViousLogging


简单易用易扩展的aop日志spring-boot-starter组件



## 使用方法
1. 下载代码，本地编译install
2. 引入pom
```xml
<dependency>
    <groupId>cn.vious.starter</groupId>
    <artifactId>logging-spring-boot-starter</artifactId>
    <version>当前版本</version>
</dependency>
```
4. 在方法上使用@Logging注解即可

@Logging注解使用示例
```java

    @Logging(optModule = "业务模块A", optType = "新增", optDesc = "【业务模块A】新增了: {{#testDto.name}} ")
    @PostMapping("/simple")
    public ResponseEntityDto<?> log1(@RequestBody TestDto testDto) {
        return ResponseUtil.buildSuccessResponseEntityDto(testDto.toString());
    }


    @Logging(loggingBefore = true, optModule = "前置日志记录module", optType = "{{@testService.str()}} 随意拼接内容", optDesc = "支持从上下文读取值（仅后置日志支持）：\n 1.读取上下文自定义值：customVal:[{{#customVal}}] , customVal2：[{{#customVal2}}]\n2.读取方法返回值:[{{#_RESULT_}}]")
    @Logging(optModule = "默认后置日志记录module", optType = "{{@testService.str()}} 随意拼接内容", optDesc = "支持从上下文读取值（仅后置日志支持）：\n 1.读取上下文自定义值：customVal:[{{#customVal}}] , customVal2：[{{#customVal2}}]\n2.读取方法返回值:[{{#_RESULT_}}]")
    @GetMapping("/test2")
    public ResponseEntityDto<?> log2() {
        LoggingContext.put("customVal", "controller业务值，在方法执行中写入上下文");
        return ResponseUtil.buildSuccessResponseEntityDto(testService.logContext());
    }

    
    // @Logging(optModule = "module", optType = "{{@exceptionController.str()}}", optDesc = "可同时解析多个模板：\n1.String返回值：[{{@exceptionController.str()}}]，\n2.DTO返回值:[{{@exceptionController.dto()}}],\n3.DTO返回值的属性取值：[{{@exceptionController.dto().name}}],\n4.带参方法：[{{@exceptionController.showI(#i)}}] \n")
    // @Logging(condition = "#i > 2 ? 'true' : 'false'",optModule = "module", optType = "{{@exceptionController.str()}}", optDesc = "可同时解析多个模板：\n1.String返回值：[{{@exceptionController.str()}}]，\n2.DTO返回值:[{{@exceptionController.dto()}}],\n3.DTO返回值的属性取值：[{{@exceptionController.dto().name}}],\n4.带参方法：[{{@exceptionController.showI(#i)}}] \n")
    @Logging(condition = "#i > 2 ? 'true' : 'false'", optModule = "module", optType = "{{@testService.str()}} hello1", optDesc = "可同时解析多个模板：\n1.String返回值：[{{@testService.str()}}]，\n2.DTO返回值:[{{@testService.dto()}}],\n3.DTO返回值的属性取值：[{{@testService.dto().name}}],\n4.带参方法：[{{@testService.showI(#i)}}] \n")
    @Logging(optModule = "module", optType = "{{@testService.str()}} hello2", optDesc = "可同时解析多个模板：\n1.String返回值：[{{@testService.str()}}]，\n2.DTO返回值:[{{@testService.dto()}}],\n3.DTO返回值的属性取值：[{{@testService.dto().name}}],\n4.带参方法：[{{@testService.showI(#i)}}] \n")
    @GetMapping("/unknown")
    public ResponseEntityDto<?> log3(@NotNull Integer i) {
        return ResponseUtil.buildSuccessResponseEntityDto(testService.unknownException(i));
    }

```



## 进阶使用

- 配置`vious.logging.to-xxx`，以改变默认日志消费者(LoggingConsumer)的行为
  - 例如：`vious.logging.to-stdout.enable=false` , 使生成的日志不在控制台打印
- 当同时激活多个日志消费者，在新日志产生时，会使用异步线程池向这些日志消费者依次发送该日志
- 日志消费者列表可完全自定义
  - 向spring容器中注入返回值类型为`List<LoggingConsumer>`的Bean即可接管日志消费者，在此情况下，默认的日志消费者不再生效。



## 后续计划

- 发布到maven中央仓库
- 扩展LoggingConsumer
  - 支持Kafka
  - 支持ElasticSearch
  - 支持AMQP
- 提高健壮性