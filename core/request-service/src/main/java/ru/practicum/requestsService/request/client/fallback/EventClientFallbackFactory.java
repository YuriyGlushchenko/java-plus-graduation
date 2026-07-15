package ru.practicum.requestsService.request.client.fallback;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.common.dto.events.EventBaseDto;
import ru.practicum.requestsService.request.client.EventClient;

@Slf4j
@Component
public class EventClientFallbackFactory implements FallbackFactory<EventClient> {

    @Override
    public EventClient create(Throwable cause) {
        return new EventClient() {

            @Override
            public EventBaseDto getBaseEventInfo(long id) {
                // 404 — событие не найден — возвращаем null
                if (cause instanceof FeignException.NotFound) {
                    log.debug("Event not found: eventId={}", id);
                    return null;
                }

                // другие ошибки (500)
                log.error("Event service error for eventId={}: {}", id, cause.getMessage());
                throw new RuntimeException("Event service is currently unavailable", cause);
            }

        };
    }
}