package com.lht.controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamResponseHandler;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;


@RestController
public class ControllerV3 {

    @GetMapping(path = "/stream-flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamFlux() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> "Flux - " + LocalTime.now().toString());
    }



    @GetMapping("/stream-sse-mvc")
    public SseEmitter streamSseMvc() {
        SseEmitter emitter = new SseEmitter();
        ExecutorService sseMvcExecutor = Executors.newSingleThreadExecutor();
        String str="火箭能够升空主要是由于牛顿第三运动定律的作用。这个定律指出,当一个物体受到一股力时,它也会对施加力的物体做出同样大小但方向相反的反作用力。\n";
        sseMvcExecutor.execute(() -> {
            try {
                for (int i = 0; i<str.length(); i++) {
                    SseEmitter.SseEventBuilder event = SseEmitter.event()
                            .data("SSE MVC - " + str)
                            .id(String.valueOf(i))
                            .name("sse event - mvc");
                    emitter.send(event);
                    Thread.sleep(1000);
                }
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }


    @GetMapping("/stream-sse")
    public Flux<ServerSentEvent<String>> streamEvents() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> ServerSentEvent.<String> builder()
                        .id(String.valueOf(sequence))
                        .event("periodic-event")
                        .data("SSE - " + LocalTime.now().toString())
                        .build());
    }



    @GetMapping("/stream_str")
    public SseEmitter handleStream_str() {
        SseEmitter emitter = new SseEmitter();

        // 在单独的线程中异步发送数据
        String str="火箭能够升空主要是由于牛顿第三运动定律的作用。这个定律指出,当一个物体受到一股力时,它也会对施加力的物体做出同样大小但方向相反的反作用力。\n";
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                for (char c : str.toCharArray()) {
//                    emitter.send(SseEmitter.event().data(String.valueOf(c))); //返回包含data: 和换行 data:要
//                    emitter.send(SseEmitter.event().data(String.valueOf(c), MediaType.TEXT_PLAIN));
                    emitter.send(String.valueOf(c), MediaType.TEXT_PLAIN);
                    Thread.sleep(500); // 模拟数据生成延迟
                }
                emitter.complete(); // 发送完成
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @GetMapping("/real_stream")
    public SseEmitter real_stream() {
        SseEmitter emitter = new SseEmitter();
        String prompt = "为什么火箭会升空?请用中文回答";
        var silent = false;

        BedrockRuntimeAsyncClient client = BedrockRuntimeAsyncClient.builder()
                .region(Region.US_WEST_2)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();

        var finalCompletion = new AtomicReference<>("");

        JSONObject content = new JSONObject();
        content.put("type", "text");
        content.put("text", prompt);

        JSONArray contentArray = new JSONArray();
        contentArray.put(content);

        JSONObject messages = new JSONObject();
        messages.put("role", "user");
        messages.put("content", contentArray);

        JSONArray arrayElementOneArray = new JSONArray();
        arrayElementOneArray.put(messages);
        var payload = new JSONObject()
                .put("anthropic_version", "bedrock-2023-05-31")
                .put("max_tokens", 200)
                .put("system", "You are an AI bot")
                .put("messages", arrayElementOneArray)
                .toString();

        var request = InvokeModelWithResponseStreamRequest.builder()
                .body(SdkBytes.fromUtf8String(payload))
                .modelId(CLAUDE3_SONNET)
                .contentType("application/json")
                .accept("application/json")
                .build();




        Executors.newSingleThreadExecutor().execute(() -> {

        var visitor = InvokeModelWithResponseStreamResponseHandler.Visitor.builder()
                .onChunk(chunk -> {
                    var json = new JSONObject(chunk.bytes().asUtf8String());
                    Iterator<String> iterator = json.keys();
                    String key = null;
                    while (iterator.hasNext()) {
                        key = iterator.next();
                        if (key.equals("delta")) {
                            var inner = new JSONObject(json.getJSONObject("delta").toString());
                            Iterator<String> iterator1 = inner.keys();
                            String key1 = "";
                            while (iterator1.hasNext()) {
                                key1 = iterator1.next();
                                if (key1.equals("text")) {
                                    var completion = inner.get("text");
                                    finalCompletion.set(finalCompletion.get() + completion);
                                    if (!silent) {
                                        System.out.print(completion);

                                            try {
                                                emitter.send(SseEmitter.event().data(completion));
                                            } catch (Exception e) {
                                                emitter.completeWithError(e);
                                            }
                                    }
                                }
                            }
                        }

                    }
                })
                .build();



        var handler = InvokeModelWithResponseStreamResponseHandler.builder()
                .onEventStream(stream -> stream.subscribe(event -> event.accept(visitor)))
                .onComplete(() -> {
                    emitter.complete(); // 发送完成
                })
                .onError(e -> System.out.println("\n\nError: " + e.getMessage()))
                .build();

        client.invokeModelWithResponseStream(request, handler).join();
        });



        return emitter;
    }


    @GetMapping("/stream")
    public SseEmitter handleStream() {
        SseEmitter emitter = new SseEmitter();
        String prompt = "为什么火箭会升空?请用中文回答";
        var silent = false;
        String str= invokeClaude3(CLAUDE3_SONNET,prompt,silent);
//        invokeClaude3Stream
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                for (char c : str.toCharArray()) {
                    emitter.send(String.valueOf(c), MediaType.TEXT_PLAIN);
                    Thread.sleep(100); // 模拟数据生成延迟
                }
                emitter.complete(); // 发送完成
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    private static void sendDataToSseEmitter(SseEmitter emitter, String data) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                emitter.send(SseEmitter.event().data(data));
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
    }



    private static final String CLAUDE3_SONNET = "anthropic.claude-3-sonnet-20240229-v1:0";

    public static String invokeClaude3(String modelId, String prompt, boolean silent) {

        BedrockRuntimeAsyncClient client = BedrockRuntimeAsyncClient.builder()
                .region(Region.US_WEST_2)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();

        var finalCompletion = new AtomicReference<>("");

        JSONObject content = new JSONObject();
        content.put("type", "text");
        content.put("text", prompt);

        JSONArray contentArray = new JSONArray();
        contentArray.put(content);

        JSONObject messages = new JSONObject();
        messages.put("role", "user");
        messages.put("content", contentArray);

        JSONArray arrayElementOneArray = new JSONArray();
        arrayElementOneArray.put(messages);
        var payload = new JSONObject()
                .put("anthropic_version", "bedrock-2023-05-31")
                .put("max_tokens", 200)
                .put("system", "You are an AI bot")
                .put("messages", arrayElementOneArray)
                .toString();

        var request = InvokeModelWithResponseStreamRequest.builder()
                .body(SdkBytes.fromUtf8String(payload))
                .modelId(modelId)
                .contentType("application/json")
                .accept("application/json")
                .build();

        var visitor = InvokeModelWithResponseStreamResponseHandler.Visitor.builder()
                .onChunk(chunk -> {
                    var json = new JSONObject(chunk.bytes().asUtf8String());
                    //System.out.print(json.getString("type"));
                    if (json.getString("type").equals("content_block_delta")){
                        //var completion = json.getJSONArray("delta").toString();
                        var completion = json.getJSONObject("delta").getString("text");
                        finalCompletion.set(finalCompletion.get() + completion);
                        if (!silent) {
                            System.out.print(completion);
                        }
                    }

                })
                .build();

        var handler = InvokeModelWithResponseStreamResponseHandler.builder()
                .onEventStream(stream -> stream.subscribe(event -> event.accept(visitor)))
                .onComplete(() -> {
                })
                .onError(e -> System.out.println("\n\nError: " + e.getMessage()))
                .build();

        client.invokeModelWithResponseStream(request, handler).join();

        return finalCompletion.get();
    }



    public static String invokeClaude3Stream(String modelId, String prompt, boolean silent) {

        BedrockRuntimeAsyncClient client = BedrockRuntimeAsyncClient.builder()
                .region(Region.US_WEST_2)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();

        var finalCompletion = new AtomicReference<>("");

        JSONObject content = new JSONObject();
        content.put("type", "text");
        content.put("text", prompt);

        JSONArray contentArray = new JSONArray();
        contentArray.put(content);

        JSONObject messages = new JSONObject();
        messages.put("role", "user");
        messages.put("content", contentArray);

        JSONArray arrayElementOneArray = new JSONArray();
        arrayElementOneArray.put(messages);
        var payload = new JSONObject()
                .put("anthropic_version", "bedrock-2023-05-31")
                .put("max_tokens", 200)
                .put("system", "You are an AI bot")
                .put("messages", arrayElementOneArray)
                .toString();

        var request = InvokeModelWithResponseStreamRequest.builder()
                .body(SdkBytes.fromUtf8String(payload))
                .modelId(modelId)
                .contentType("application/json")
                .accept("application/json")
                .build();

        var visitor = InvokeModelWithResponseStreamResponseHandler.Visitor.builder()
                .onChunk(chunk -> {
                    var json = new JSONObject(chunk.bytes().asUtf8String());
                    //System.out.print(json.getString("type"));
                    if (json.getString("type").equals("content_block_delta")){
                        //var completion = json.getJSONArray("delta").toString();
                        var completion = json.getJSONObject("delta").getString("text");
                        finalCompletion.set(finalCompletion.get() + completion);
                        if (!silent) {
                            System.out.print(completion);
                        }
                    }

                })
                .build();

        var handler = InvokeModelWithResponseStreamResponseHandler.builder()
                .onEventStream(stream -> stream.subscribe(event -> event.accept(visitor)))
                .onComplete(() -> {
                })
                .onError(e -> System.out.println("\n\nError: " + e.getMessage()))
                .build();

        client.invokeModelWithResponseStream(request, handler).join();

        return finalCompletion.get();
    }

}


