package ru.practicum.eventsService.event.service;

import ru.practicum.common.dto.events.EventBaseDto;

public interface InternalEventService {

    EventBaseDto findEventBaseInfoById(long id);
}
