package ru.practicum.eventsService.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.aop.annotation.Loggable;
import ru.practicum.common.dto.events.EventFullDto;
import ru.practicum.common.dto.events.EventShortDto;
import ru.practicum.eventsService.event.dto.paramDto.PublicUserEventParam;
import ru.practicum.eventsService.event.service.EventService;

import java.util.List;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/events")
public class PublicEventController {
    private final EventService eventService;

    @GetMapping
    @Loggable
    public List<EventShortDto> getEvents(@Valid PublicUserEventParam param,
                                         HttpServletRequest request) {

        param.setUri(request.getRequestURI());
        param.setIp(request.getRemoteAddr());

        log.debug("Public request to get events: {}", param);

        return eventService.getEventsForPublicRequests(param);
    }

    @GetMapping("/{id}")
    @Loggable
    public EventFullDto getEvent(@PathVariable long id,
                                 HttpServletRequest request,
                                 @RequestHeader("X-EWM-USER-ID") long userId) {

        log.debug("Request to get event: uri={}, ip={}, id={}", request.getRequestURI(), request.getRemoteAddr(), id);

        return eventService.findEventById(request.getRequestURI(), request.getRemoteAddr(), id, userId);
    }


    /**
     * Возвращает рекомендации мероприятий для пользователя.
     */
    @GetMapping("/recommendations")
    @Loggable
    public List<EventFullDto> getRecommendations(
            @RequestParam(defaultValue = "10") int maxResults,
            @RequestHeader("X-EWM-USER-ID") long userId) {

        log.debug("Request to get recommendations: userId={}, maxResults={}", userId, maxResults);

        return eventService.getRecommendationsForUser(userId, maxResults);
    }

    /**
     * Отправляет в Collector информацию о том, что пользователь лайкнул мероприятие.
     * Пользователь может лайкать только посещённые им мероприятия.
     */
    @PutMapping("/{eventId}/like")
    @Loggable
    public void likeEvent(
            @PathVariable long eventId,
            @RequestHeader("X-EWM-USER-ID") long userId) {

        log.debug("Request to like event: userId={}, eventId={}", userId, eventId);

        eventService.likeEvent(userId, eventId);
    }


}
