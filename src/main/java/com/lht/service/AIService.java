package com.lht.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AIService {
    SseEmitter streamSseMvc();
    SseEmitter streamClaude3();
    SseEmitter streamNovaLite();
}
