package ru.yandex.practicum;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping
    public Mono<Map<String, String>> fallback() {
        return Mono.just(Map.of(
                "status", "503",
                "message", "Service temporarily unavailable. Please try again later."
        ));
    }
}