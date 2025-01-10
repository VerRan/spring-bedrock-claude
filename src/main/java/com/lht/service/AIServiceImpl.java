package com.lht.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamResponseHandler;

import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class AIServiceImpl implements AIService {

    private static final String CLAUDE3_SONNET = "anthropic.claude-3-sonnet-20240229-v1:0";
    private static final String NOVA_LITE = "amazon.nova-lite-v1:0";

    private final ObjectMapper objectMapper;

    public AIServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public SseEmitter streamSseMvc() {
        SseEmitter emitter = new SseEmitter();
        String str = "火箭能够升空主要是由于牛顿第三运动定律的作用。这个定律指出,当一个物体受到一股力时,它也会对施加力的物体做出同样大小但方向相反的反作用力。\n";
        
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                for (int i = 0; i < str.length(); i++) {
                    SseEmitter.SseEventBuilder event = SseEmitter.event()
                            .data("SSE MVC - " + str.charAt(i))
                            .id(String.valueOf(i))
                            .name("sse event - mvc");
                    emitter.send(event);
                    Thread.sleep(1000);
                }
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }

    @Override
    public SseEmitter streamClaude3() {
        SseEmitter emitter = new SseEmitter();
        String prompt = "为什么火箭会升空?请用英文回答";

        BedrockRuntimeAsyncClient client = BedrockRuntimeAsyncClient.builder()
                .region(Region.US_WEST_2)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();

        JSONObject payload = createClaude3Payload(prompt);

        InvokeModelWithResponseStreamRequest request = InvokeModelWithResponseStreamRequest.builder()
                .body(SdkBytes.fromUtf8String(payload.toString()))
                .modelId(CLAUDE3_SONNET)
                .contentType("application/json")
                .accept("application/json")
                .build();

        processModelStream(client, request, emitter);
        return emitter;
    }

    @Override
    public SseEmitter streamNovaLite() {
        SseEmitter emitter = new SseEmitter();

        BedrockRuntimeAsyncClient client = BedrockRuntimeAsyncClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();

        ObjectNode requestBody = createNovaLitePayload();

        InvokeModelWithResponseStreamRequest request = InvokeModelWithResponseStreamRequest.builder()
                .body(SdkBytes.fromUtf8String(requestBody.toString()))
                .modelId(NOVA_LITE)
                .contentType("application/json")
                .accept("application/json")
                .build();

        processModelStream(client, request, emitter);
        return emitter;
    }

    private JSONObject createClaude3Payload(String prompt) {
        JSONObject content = new JSONObject();
        content.put("type", "text");
        content.put("text", prompt);

        JSONArray contentArray = new JSONArray();
        contentArray.put(content);

        JSONObject messages = new JSONObject();
        messages.put("role", "user");
        messages.put("content", contentArray);

        JSONArray messagesArray = new JSONArray();
        messagesArray.put(messages);

        return new JSONObject()
                .put("anthropic_version", "bedrock-2023-05-31")
                .put("max_tokens", 200)
                .put("system", "You are an AI bot")
                .put("messages", messagesArray);
    }

    private ObjectNode createNovaLitePayload() {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("schemaVersion", "messages-v1");

        ArrayNode systemList = requestBody.putArray("system");
        ObjectNode systemMessage = systemList.addObject();
        systemMessage.put("text", "你是一个广告素材识别专家");

        ArrayNode messageList = requestBody.putArray("messages");
        ObjectNode userMessage = messageList.addObject();
        userMessage.put("role", "user");
        ArrayNode contentArray = userMessage.putArray("content");
        ObjectNode textContent = contentArray.addObject();
        textContent.put("text", "你好，帮我写一首李白风格的表达冬天美景的诗歌");

        ObjectNode inferenceConfig = requestBody.putObject("inferenceConfig");
        inferenceConfig.put("max_new_tokens", 300);
        inferenceConfig.put("top_p", 0.1);
        inferenceConfig.put("top_k", 20);
        inferenceConfig.put("temperature", 0.3);

        return requestBody;
    }

    private void processModelStream(BedrockRuntimeAsyncClient client, InvokeModelWithResponseStreamRequest request, SseEmitter emitter) {
        AtomicReference<String> finalCompletion = new AtomicReference<>("");

        Executors.newSingleThreadExecutor().execute(() -> {
            InvokeModelWithResponseStreamResponseHandler.Visitor visitor = InvokeModelWithResponseStreamResponseHandler.Visitor.builder()
                    .onChunk(chunk -> {
                        JSONObject json = new JSONObject(chunk.bytes().asUtf8String());
                        processJsonChunk(json, finalCompletion, emitter);
                    })
                    .build();

            InvokeModelWithResponseStreamResponseHandler handler = InvokeModelWithResponseStreamResponseHandler.builder()
                    .onEventStream(stream -> stream.subscribe(event -> event.accept(visitor)))
                    .onComplete(emitter::complete)
                    .onError(e -> {
                        System.out.println("\n\nError: " + e.getMessage());
                        emitter.completeWithError(e);
                    })
                    .build();

            client.invokeModelWithResponseStream(request, handler).join();
        });
    }

    private void processJsonChunk(JSONObject json, AtomicReference<String> finalCompletion, SseEmitter emitter) {
        String key = json.has("delta") ? "delta" : "contentBlockDelta";
        if (json.has(key)) {
            JSONObject inner = json.getJSONObject(key);
            String textKey = key.equals("delta") ? "text" : "delta";
            if (inner.has(textKey)) {
                String completion = inner.getString(textKey);
                finalCompletion.set(finalCompletion.get() + completion);
                try {
                    emitter.send(SseEmitter.event().data(completion));
                } catch (Exception e) {
                    emitter.completeWithError(e);
                }
            }
        }
    }
}
