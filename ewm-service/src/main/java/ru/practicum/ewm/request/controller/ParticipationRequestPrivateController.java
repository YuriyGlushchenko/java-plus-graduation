package ru.practicum.ewm.request.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.service.ParticipationRequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/requests")
@Validated
@RequiredArgsConstructor
public class ParticipationRequestPrivateController {
    private final ParticipationRequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addRequest(@PathVariable long userId,
                                              @RequestParam long eventId) {
        log.debug("Request to participate in the event:  userId={}, eventId={}", userId, eventId);

        return requestService.createRequest(userId, eventId);
    }

    @GetMapping
    public List<ParticipationRequestDto> getUserParticipationRequests(@PathVariable long userId) {
        log.debug("Request for user participation requests:  userId={}", userId);

        return requestService.getUserParticipationRequests(userId);
    }

    @PatchMapping("{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable long userId,
                                                 @PathVariable long requestId) {
        log.debug("Request for cancel participation request:  userId={}, requestId={}", userId, requestId);

        return requestService.cancelRequest(userId, requestId);
    }


}
