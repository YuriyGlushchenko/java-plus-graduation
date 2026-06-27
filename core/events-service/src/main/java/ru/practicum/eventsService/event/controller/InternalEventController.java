package ru.practicum.eventsService.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.common.dto.events.EventBaseDto;
import ru.practicum.eventsService.event.service.InternalEventService;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("api/events")
public class InternalEventController implements ru.practicum.common.apiContracts.EventApiContract {

    private final InternalEventService internalEventService;

    @GetMapping("/{id}")
    @Override
    public EventBaseDto getBaseEventInfo(@PathVariable long id) {

        log.debug("Internal request to get event base info: id={}", id);

        return internalEventService.findEventBaseInfoById(id);
    }


}
