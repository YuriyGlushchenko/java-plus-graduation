package ru.practicum.eventsService.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.common.dto.events.EventBaseDto;
import ru.practicum.common.dto.events.EventFullDto;
import ru.practicum.common.dto.users.UserShortDto;
import ru.practicum.common.exceptions.exceptions.NotFoundException;
import ru.practicum.eventsService.event.dto.EventMapper;
import ru.practicum.eventsService.event.model.Event;
import ru.practicum.eventsService.event.repository.EventRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalEventServiceImpl implements InternalEventService {
    private final EventRepository eventRepository;
    @Override
    public EventBaseDto findEventBaseInfoById(long id) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " not found"));

        return EventMapper.toEventBaseDto(event);
    }
}
