# BedrockAIStreamer: AWS Bedrock AI Models Integration with Streaming

This project demonstrates how to integrate AWS Bedrock AI models (Claude3 and Nova Lite) into a Spring Boot application, implementing streaming inference functionality.

## Project Background

As machine learning models continue to evolve and their application scenarios expand, businesses increasingly need to integrate these models into their applications to provide more intelligent and personalized services. This project showcases how to use the AWS Bedrock service to integrate Claude3 and Nova Lite models, implementing streaming response patterns.

## Features

- **Streaming Responses**: Implements real-time streaming responses using `InvokeModelWithResponseStream`
- **Spring Boot Integration**: Provides API endpoints based on Spring MVC
- **SSE Support**: Uses Server-Sent Events to implement streaming data push
- **Multiple AI Models**: Supports both Claude3 and Nova Lite models from AWS Bedrock

## Project Structure

```
src/main/java/com/lht/
├── Application.java           # Application entry point
├── controller/
│   └── AIController.java      # API controller for AI endpoints
├── service/
│   ├── AIService.java         # Interface for AI service
│   └── AIServiceImpl.java     # Implementation of AI service
```

## Requirements

- Java 17 or higher
- Maven
- AWS account with Bedrock access permissions
- Configured AWS credentials

## Quick Start

1. Clone the project:
```bash
git clone https://github.com/yourusername/bedrock-ai-streamer.git
```

2. Configure AWS credentials

3. Build the project:
```bash
mvn clean install
```

4. Run the application:
```bash
mvn spring-boot:run
```

## Usage Examples

### Streaming API Endpoints

- `/stream-sse-mvc`: Simple SSE streaming example
- `/claude3_stream`: Streaming responses from Claude3 model
- `/nova_stream`: Streaming responses from Nova Lite model

Example usage with curl:
```bash
curl http://localhost:8080/claude3_stream
```

## Configuration

The project configuration is managed through the `application.yaml` file, which includes:
- AWS region settings
- Model configurations
- Application-specific settings

## Logging

The project uses Spring Boot's default logging. You can configure logging levels in the `application.yaml` file.

## Common Issues

### Thread Safety
To ensure the order and consistency of inference results:
1. SSE and inference code are executed in the same thread
2. Appropriate thread synchronization mechanisms are used
3. Pay attention to exception handling in asynchronous operations

## References

- [Amazon Bedrock Documentation](https://docs.aws.amazon.com/bedrock/)
- [Spring Boot SSE Guide](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-async.html)
- [Claude3 API Documentation](https://docs.anthropic.com/claude/reference/getting-started-with-the-api)

## Contributing

Contributions are welcome! Please feel free to submit issues and pull requests to improve the project.

## License

[Add license information]
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
