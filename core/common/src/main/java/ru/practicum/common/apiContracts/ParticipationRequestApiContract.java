package ru.practicum.common.apiContracts;

import org.springframework.web.bind.annotation.*;
import ru.practicum.common.dto.participationRequest.EventRequestStatusUpdateRequest;
import ru.practicum.common.dto.participationRequest.EventRequestStatusUpdateResult;
import ru.practicum.common.dto.participationRequest.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

public interface ParticipationRequestApiContract {
    @GetMapping("/event/{eventId}/count")
    Long getConfirmedRequestsCount(@PathVariable Long eventId);

    @GetMapping("/event/{eventId}")
    List<ParticipationRequestDto> getRequestsByEventId(@PathVariable Long eventId);

    @PatchMapping("/event/{eventId}")
    EventRequestStatusUpdateResult updateRequestStatuses(
            @PathVariable Long eventId,
            @RequestBody EventRequestStatusUpdateRequest request);

    @PostMapping("/events/count")
    Map<Long, Long> getConfirmedRequestsCounts(@RequestBody List<Long> eventIds);
}
