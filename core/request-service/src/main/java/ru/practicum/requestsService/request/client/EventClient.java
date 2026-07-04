package ru.practicum.requestsService.request.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.common.apiContracts.EventApiContract;
import ru.practicum.requestsService.request.client.fallback.EventClientFallbackFactory;

@FeignClient(name = "event-service", fallbackFactory = EventClientFallbackFactory.class)
public interface EventClient extends EventApiContract {
}
