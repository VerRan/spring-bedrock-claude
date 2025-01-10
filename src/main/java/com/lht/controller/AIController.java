package com.lht.controller;

import com.lht.service.AIService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/stream-sse-mvc")
    public SseEmitter streamSseMvc() {
        return aiService.streamSseMvc();
    }

    @GetMapping(value = "/claude3_stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamClaude3() {
        return aiService.streamClaude3();
    }

    @GetMapping(value = "/nova_stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNovaLite() {
        return aiService.streamNovaLite();
    }
}
