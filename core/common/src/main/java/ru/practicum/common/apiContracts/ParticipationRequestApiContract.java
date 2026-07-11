package ru.practicum.common.apiContracts;

import org.springframework.web.bind.annotation.*;
import ru.practicum.common.dto.participationRequest.EventRequestStatusUpdateRequest;
import ru.practicum.common.dto.participationRequest.EventRequestStatusUpdateResult;
import ru.practicum.common.dto.participationRequest.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

public interface ParticipationRequestApiContract {

    @GetMapping("/api/requests/event/{eventId}")
    List<ParticipationRequestDto> getRequestsByEventId(@PathVariable Long eventId);

    @PatchMapping("/api/requests/event/{eventId}")
    EventRequestStatusUpdateResult updateRequestStatuses(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int limit,
            @RequestBody EventRequestStatusUpdateRequest request);

    @PostMapping("/api/requests/events/count")
    Map<Long, Long> getConfirmedRequestsCounts(@RequestBody List<Long> eventIds);

    @GetMapping("/api/requests/check")
    boolean hasConfirmedParticipation(@RequestParam Long eventId,
                                      @RequestParam Long userId);
}
