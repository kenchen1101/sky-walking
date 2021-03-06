Sky Walking
==========

<img src="http://wu-sheng.github.io/sky-walking/images/skywalking.png" alt="Sky Walking logo" height="90px" align="right" />

SkyWalking: Large-Scale Distributed Systems Tracing Infrastructure, 是一个对JAVA分布式应用程序集群的业务运行情况进行追踪、告警和分析的系统。

[![Build Status](https://travis-ci.org/wu-sheng/sky-walking.svg?branch=master)](https://travis-ci.org/wu-sheng/sky-walking)

* 核心理论为[Google Dapper论文：Dapper, a Large-Scale Distributed Systems Tracing Infrastructure](http://research.google.com/pubs/pub36356.html),英语有困难的同学可参考[国内翻译](http://duanple.blog.163.com/blog/static/70971767201329113141336/)
* 本分析系统能通过不修改或少量修改代码的模式，对现有的JAVA应用或J2EE应用进行监控和数据收集，并针对应用进场进行准实时告警。此外提供大量的调用性能分析功能，解决目前的监控系统主要监控进程、端口而非应用实际性能的问题。
* 支持国内常用的dubbo以及dubbox等常见RPC框架，支持应用异常的邮件告警
* skywalking-sdk层面提供的埋点API，同步阻塞访问效率小于100μs
* 通过[byte-buddy](https://github.com/raphw/byte-buddy)，部分插件将通过动态字节码机制，避免代码侵入性，完成监控。动态代码模式埋点，同步阻塞访问效率应在200-300μs

|插件名称|配置文件支持|动态代码机制|代码侵入模式|备注|
| ----------- |---------| ----------|----------|----------|
|web-plugin|web.xml| - | - | - |
|dubbo-plugin| dubbo/dubbox配置文件 | - | - | - |
|spring-plugin| spring配置文件 | - | - | - |
|jdbc-plugin| jdbc配置文件 | - | - | - |
|mysql-plugin| - | YES | - | - |
|httpClient-4.x-plugin| - | YES | - | - |
|httpClient-4.x-plugin-dubbox-rest-attachment| - | YES | - | 需引用httpClient-4.x-plugin |
|jedis-2.x-plugin| - | YES | - | - |
|~~httpclient-4.2.x-plugin~~| - | - | YES | 需要使用新提供的httpClient包装对象 |
|~~httpclient-4.3.x-plugin~~| - | - | YES | 需要使用新提供的httpClient包装对象 |

* 删除插件为最新版本不推荐使用的插件

# 新版本能力规划
* 提供一定的日志数据分析和展现能力，减少或者避免使用团队的二次开发

# 主要贡献者
* 吴晟 &nbsp;&nbsp;[亚信](http://www.asiainfo.com/) wusheng@asiainfo.com
* 张鑫 &nbsp;&nbsp;[亚信](http://www.asiainfo.com/) zhangxin10@asiainfo.com

# 交流
* 联系邮箱：wu.sheng@foxmail.com
* QQ群：392443393，请注明“Sky Walking交流”
* 谁在使用Sky Walking?[点击进入](https://github.com/wu-sheng/sky-walking/issues/34)。同时请各位使用者反馈下，都在哪些项目中使用。

# 整体架构图
![整体架构图](http://wu-sheng.github.io/sky-walking/sample-code/images/skywalkingClusterDeploy.jpeg)

# 典型页面展现
## 实时调用链路
* 实时链路追踪展现
![追踪连路图1](http://wu-sheng.github.io/sky-walking/sample-code/screenshoot/callChain.png)
* 实时链路追踪详细信息查看
![追踪连路图2](http://wu-sheng.github.io/sky-walking/sample-code/screenshoot/callChainDetail.png)
* 实时链路追踪日志查看
![追踪连路图3](http://wu-sheng.github.io/sky-walking/sample-code/screenshoot/callChainLog.png)
* 实时链路异常告警邮件
![告警邮件](http://wu-sheng.github.io/sky-walking/sample-code/screenshoot/alarmMail.jpg)

## 分析汇总

# Quick Start
## 编译与部署
- 参考《[代码编译部署说明](BUILD_DOC.md)》

## 引入核心SDK
无论试用哪种插件，都必须引入
```xml
<!-- API日志输出，客户端可指定所需的log4j2版本 -->
<!-- 2.4.1为开发过程所选用版本 -->
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-core</artifactId>
    <version>2.4.1</version>
</dependency>
<!-- 监控api，可监控插件不支持的调用 -->
<dependency>
    <groupId>com.ai.cloud</groupId>
    <artifactId>skywalking-api</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## 使用全新的main class。原main class，以及参数作为参数传入
```shell
#原进程启动命令：
java com.company.product.Startup arg0 arg1

#全新的进程启动命令：
java com.ai.cloud.skywalking.plugin.TracingBootstrap com.company.product.Startup arg0 arg1
```

## 根据所需插件，配置应用程序
- 参考《[SDK用户指南](skywalking-sdk-plugin)》
- 注意：插件不会引用所需的第三方组件（如Spring、dubbo、dubbox等），请自行引入所需的版本。


## 下载并设置授权文件
- 注册并登陆过skywalking-webui，创建应用。（一个用户代表一个逻辑集群，一个应用代表一个服务集群。如前后端应用应该设置两个应用，但归属一个用户）
- 下载授权文件，并在运行时环境中，将授权文件加入到CLASSPATH中

## 在运行时环境中设置环境变量
```
export SKYWALKING_RUN=true
```
- 设置完成后，可以在当前环境中启动业务应用系统

## 通过扩展log4j或log4j2，在应用日志中，显示trace-id
### log4j
- 编译并发布skywalking-log/log4j-1.x-plugin
```xml
<dependency>
    <groupId>com.ai.cloud</groupId>
    <artifactId>skywalking-log4j-1.x-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
- 配置log4j配置文件
```properties
log4j.appender.A1.layout=com.ai.cloud.skywalking.plugin.log.log4j.v1.x.TraceIdPatternLayout
#%x为traceid的转义符
log4j.appender.A1.layout.ConversionPattern=[%x] %-d{yyyy-MM-dd HH:mm:ss.SSS} %c %n[%p] %n%m%n
```

### log4j2
- 编译并发布skywalking-log/log4j-2.x-plugin
- 引用所需的日志插件
```xml
<dependency>
    <groupId>com.ai.cloud</groupId>
    <artifactId>skywalking-log4j-2.x-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
- 配置log4j2配置文件
```xml
<!--%tid为traceid的转义符-->
<PatternLayout  pattern="%d{HH:mm:ss.SSS} [%tid] [%t] %-5level %logger{36} - %msg%n"/>
```

- 日志示例
```
#tid:N/A，代表环境设置不正确或监控已经关闭
#tid: ,代表测试当前访问不在监控范围
#tid:1.0a2.1453065000002.c3f8779.27878.30.184，标识此次访问的tid信息，示例如下
[DEBUG] Returning handler method [public org.springframework.web.servlet.ModelAndView com.ai.cloud.skywalking.example.controller.OrderSaveController.save(javax.servlet.http.HttpServletRequest)] TID:1.0a2.1453192613272.2e0c63e.11144.58.1 2016-01-19 16:36:53.288 org.springframework.beans.factory.support.DefaultListableBeanFactory 
```

## 如何在追踪日志中记录日志上下文
- 使用sky walking提供的专用API，可以将日志保存到追踪日志中。示例如下：
```java
String businessKey = "phoneNumber:" + phoneNumber + ",resourceId:" + resourceId + ",mail:" + mail;
BusinessKeyAppender.setBusinessKey2Trace(businessKey);
```

## 如何在代码中获取traceid
- 通过API获取traceid
```java
Tracing.getTraceId();
```

## 还有其他方式获取traceid么？
- 通过web应用的http调用入口，通过返回的header信息，找到此次调用的traceid。前提：此web应用的url，已经使用skywalking进行监控。

# 源代码说明
* [追踪日志明细存储结构说明](https://github.com/wu-sheng/sky-walking/blob/master/skywalking-server/doc/hbase_table_desc.md)
