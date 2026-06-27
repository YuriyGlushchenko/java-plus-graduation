package ru.practicum.eventsService.event.service;

import ru.practicum.common.dto.events.EventBaseDto;
import ru.practicum.common.dto.events.EventFullDto;

public interface InternalEventService {

    EventBaseDto findEventBaseInfoById(long id);
}
