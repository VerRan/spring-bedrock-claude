//package com.lht.test;
//
//import org.springframework.core.ParameterizedTypeReference;
//import org.springframework.http.codec.ServerSentEvent;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Flux;
//
//import java.time.LocalTime;
//import java.util.logging.Logger;
//
//public class SseTest {
////    Logger logger =Logger.getLogger("SseTest");
//private static final Logger logger = Logger.getLogger(SseTest.class.getName());
//    public void consumeServerSentEvent() {
//        WebClient client = WebClient.create("http://localhost:8080/sse-server");
//        ParameterizedTypeReference<ServerSentEvent<String>> type
//                = new ParameterizedTypeReference<ServerSentEvent<String>>() {};
//
//        Flux<ServerSentEvent<String>> eventStream = client.get()
//                .uri("/stream-sse")
//                .retrieve()
//                .bodyToFlux(type);
//
//        eventStream.subscribe(
//                content -> logger.info("Time: {} - event: name[{}], id [{}], content[{}] ",
//                        LocalTime.now(), content.event(), content.id(), content.data()),
//                error -> logger.error("Error receiving SSE: {}", error),
//                () -> logger.info("Completed!!!"));
//    }
//}
