package ru.practicum.eventsService.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.common.apiContracts.ParticipationRequestApiContract;
import ru.practicum.eventsService.client.fallback.ParticipationRequestClientFallbackFactory;

@FeignClient(name = "request-service", fallbackFactory = ParticipationRequestClientFallbackFactory.class)
public interface ParticipationRequestClient extends ParticipationRequestApiContract {
}
