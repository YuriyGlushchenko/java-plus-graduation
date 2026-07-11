package ru.practicum.eventsService.client.fallback;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.common.dto.participationRequest.EventRequestStatusUpdateRequest;
import ru.practicum.common.dto.participationRequest.EventRequestStatusUpdateResult;
import ru.practicum.common.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.common.exceptions.exceptions.ConditionsNotMetException;
import ru.practicum.common.exceptions.exceptions.NotFoundException;
import ru.practicum.eventsService.client.ParticipationRequestClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ParticipationRequestClientFallbackFactory implements FallbackFactory<ParticipationRequestClient> {

    @Override
    public ParticipationRequestClient create(Throwable cause) {
        return new ParticipationRequestClient() {

            @Override
            public List<ParticipationRequestDto> getRequestsByEventId(Long eventId) {
                log.error("Request service unavailable for eventId={}", eventId, cause);

                ParticipationRequestDto fallbackDto = ParticipationRequestDto.builder()
                        .id(-1L)
                        .created(null)
                        .event(eventId)
                        .requester(null)
                        .status(null)
                        .build();

                return List.of(fallbackDto);
            }

            @Override
            public EventRequestStatusUpdateResult updateRequestStatuses(
                    Long eventId,
                    int limit,
                    EventRequestStatusUpdateRequest request) {

                if (cause instanceof FeignException ex) {

                    if (ex.status() == 409) {
                        throw new ConditionsNotMetException(
                                "The participant limit has been reached or request status is not PENDING"
                        );
                    }

                    if (ex.status() == 404) {
                        throw new NotFoundException("Request or event not found for id=" + eventId);
                    }

                    // 5xx — fallback
                    if (ex.status() >= 500) {
                        log.error("Request service internal error for eventId={}, status={}", eventId, ex.status());
                        return new EventRequestStatusUpdateResult(
                                List.of(ParticipationRequestDto.builder().id(-1L).build()),
                                List.of(ParticipationRequestDto.builder().id(-1L).build())
                        );
                    }
                }

                // Другие ошибки — fallback
                log.error("Request service unavailable for eventId={}", eventId, cause);
                return new EventRequestStatusUpdateResult(
                        List.of(ParticipationRequestDto.builder().id(-1L).build()),
                        List.of(ParticipationRequestDto.builder().id(-1L).build())
                );
            }

            @Override
            public Map<Long, Long> getConfirmedRequestsCounts(List<Long> eventIds) {
                log.error("Request service unavailable for eventIds={}", eventIds, cause);

                Map<Long, Long> fallbackMap = new HashMap<>();
                eventIds.forEach(id -> fallbackMap.put(id, -1L));
                return fallbackMap;
            }

            @Override
            public boolean hasConfirmedParticipation(Long eventId, Long userId) {
                return false;
            }
        };
    }
}