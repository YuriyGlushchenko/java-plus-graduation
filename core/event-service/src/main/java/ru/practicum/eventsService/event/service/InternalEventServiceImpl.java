package ru.practicum.eventsService.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.common.dto.events.EventBaseDto;
import ru.practicum.common.exceptions.exceptions.NotFoundException;
import ru.practicum.eventsService.event.dto.EventMapper;
import ru.practicum.eventsService.event.model.Event;
import ru.practicum.eventsService.event.repository.EventRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalEventServiceImpl implements InternalEventService {
    private final EventRepository eventRepository;

    /**
     * Возвращает полную базовую информацию о событии без счетчиков просмотров, комментариев и заявок.
     */
    @Override
    public EventBaseDto findEventBaseInfoById(long id) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " not found"));

        return EventMapper.toEventBaseDto(event);
    }
}
