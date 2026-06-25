package ru.practicum.ewm.event.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/events")
@Validated
@RequiredArgsConstructor
public class PrivateEventController {
    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable long userId,
                                 @RequestBody @Valid NewEventDto newEventDto) {
        log.debug("Request to add new event:  userId={}", userId);

        return eventService.createEvent(userId, newEventDto);
    }

    @GetMapping
    public List<EventShortDto> getUserEvents(@PathVariable long userId,
                                             @RequestParam(defaultValue = "0") @Min(0) int from,
                                             @RequestParam(defaultValue = "10") @Positive int size) {
        log.debug("Request to get user events:  userId={}", userId);

        return eventService.getUserEvents(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getUserEvent(@PathVariable long userId,
                                     @PathVariable long eventId) {
        log.debug("Request to get user event:  userId={}, eventId={}", userId, eventId);

        return eventService.findUserEventByEventId(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateUserEvent(@PathVariable long userId,
                                        @PathVariable long eventId,
                                        @RequestBody @Valid UpdateEventUserRequest body) {
        log.debug("Request to update user event:  userId={}, eventId={}", userId, eventId);

        return eventService.updateUserEvent(userId, eventId, body);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getParticipationRequests(@PathVariable long userId,
                                                                  @PathVariable long eventId) {
        log.debug("Request to get Participation Requests for user event:  userId={}, eventId={}", userId, eventId);

        return eventService.getParticipationRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatuses(@PathVariable long userId,
                                                                @PathVariable long eventId,
                                                                @RequestBody EventRequestStatusUpdateRequest request) {
        log.debug("Request to update Participation Requests statuses:  userId={}, eventId={}", userId, eventId);

        return eventService.updateRequestStatuses(userId, eventId, request);
    }

}
