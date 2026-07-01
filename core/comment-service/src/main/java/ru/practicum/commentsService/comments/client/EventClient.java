package ru.practicum.commentsService.comments.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.commentsService.comments.client.fallback.EventClientFallbackFactory;
import ru.practicum.common.apiContracts.EventApiContract;

@FeignClient(name = "event-service", fallbackFactory = EventClientFallbackFactory.class)
public interface EventClient extends EventApiContract {
}
