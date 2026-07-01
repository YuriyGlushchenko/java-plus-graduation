package ru.practicum.requestsService.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.dto.participationRequest.EventRequestStatusUpdateRequest;
import ru.practicum.common.dto.participationRequest.EventRequestStatusUpdateResult;
import ru.practicum.common.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.requestsService.request.service.ParticipationRequestService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class InternalRequestController implements ru.practicum.common.apiContracts.ParticipationRequestApiContract {

    private final ParticipationRequestService requestService;

    @GetMapping("/event/{eventId}")
    @Override
    public List<ParticipationRequestDto> getRequestsByEventId(@PathVariable Long eventId) {
        log.debug("Internal request: get requests for eventId={}", eventId);
        return requestService.getRequestsByEventId(eventId);
    }

    /**
     * Внутренний эндпоинт для изменения статуса заявок
     */
    @PatchMapping("/event/{eventId}")
    @Override
    public EventRequestStatusUpdateResult updateRequestStatuses(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int limit,
            @RequestBody EventRequestStatusUpdateRequest request) {
        log.debug("Internal request: update request statuses for eventId={}", eventId);
        return requestService.updateRequestStatuses(eventId, limit, request);
    }

    /**
     * Внутренний эндпоинт для получения количества подтвержденных заявок по списку событий
     */
    @PostMapping("/events/count")
    @Override
    public Map<Long, Long> getConfirmedRequestsCounts(@RequestBody List<Long> eventIds) {
        log.debug("Internal request: get confirmed requests counts for eventIds={}", eventIds);
        return requestService.getConfirmedRequestsCounts(eventIds);
    }
}