# Cloud3-POC: Amazon Bedrock Claude3 流式推理集成

本项目展示了如何在 Spring Boot 应用程序中集成 Amazon Bedrock Claude3 模型，实现同步、异步和流式推理功能。

## 项目背景

随着机器学习模型的不断发展和应用场景的扩展，企业越来越需要将模型集成到自己的应用程序中，以提供更智能和个性化的服务。本项目展示了如何使用 Amazon Bedrock 服务来集成 Claude3 模型，并实现不同的调用模式。

## 功能特点

- **同步调用**: 通过 `InvokeModel` 实现直接模型调用
- **异步调用**: 通过 `InvokeModelAsync` 实现非阻塞式调用
- **流式响应**: 通过 `InvokeModelWithResponseStream` 实现实时流式响应
- **Spring Boot 集成**: 提供基于 Spring MVC 的 API 端点
- **SSE 支持**: 使用 Server-Sent Events 实现流式数据推送

## 项目结构

```
src/main/java/com/lht/
├── Application.java                    # 应用程序入口
├── app/
│   ├── BedrockRuntimeUsageDemo.java   # Bedrock运行时示例
│   └── Claude3.java                   # Claude3 模型实现
├── controller/
│   └── ControllerV3.java             # API控制器
└── runtime/
    ├── AppController.java            # 应用控制器
    ├── InvokeModel.java             # 同步调用实现
    ├── InvokeModelAsync.java        # 异步调用实现
    └── InvokeModelWithResponseStream.java # 流式响应实现
```

## 环境要求

- Java 8 或更高版本
- Maven
- AWS 账号和 Bedrock 访问权限
- 配置好的 AWS 凭证

## 快速开始

1. 克隆项目:
```bash
git clone https://github.com/yourusername/Cloud3-POC.git
```

2. 配置 AWS 凭证

3. 构建项目:
```bash
mvn clean install
```

## 使用示例

### 同步调用
```java
InvokeModel invokeModel = new InvokeModel();
String response = invokeModel.invoke(prompt);
```

### 异步调用
```java
InvokeModelAsync asyncModel = new InvokeModelAsync();
CompletableFuture<String> future = asyncModel.invokeAsync(prompt);
```

### 流式调用
```java
InvokeModelWithResponseStream streamingModel = new InvokeModelWithResponseStream();
streamingModel.invokeStream(prompt, response -> {
    // 处理流式响应
});
```

### SSE 控制器示例
```java
@RestController
public class ControllerV3 {
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamResponse() {
        SseEmitter emitter = new SseEmitter();
        // 实现流式响应逻辑
        return emitter;
    }
}
```

## 配置说明

项目配置通过 `application.yaml` 文件管理，主要包括：
- AWS 区域设置
- 模型配置
- 应用程序特定配置

## 常见问题

### 依赖冲突
在集成 Amazon Bedrock 时可能遇到依赖冲突问题。解决方案：
1. 使用 Maven 依赖管理排除冲突依赖
2. 确保使用兼容的依赖版本

### 线程安全
为确保推理结果的顺序性和一致性：
1. SSE 和推理代码放在同一线程中执行
2. 使用适当的线程同步机制
3. 注意异步操作的异常处理

## 参考资源

- [Amazon Bedrock 文档](https://docs.aws.amazon.com/bedrock/)
- [Spring Boot SSE 指南](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-async.html)
- [Claude3 API 文档](https://docs.anthropic.com/claude/reference/getting-started-with-the-api)

## 日志配置

项目使用 log4j 进行日志管理，配置文件位于 `src/main/resources/log4j.properties`。

## 贡献指南

欢迎提交 Issues 和 Pull Requests 来改进项目。

## 许可证

[添加许可证信息]
