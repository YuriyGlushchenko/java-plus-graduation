package ru.practicum.eventsService.event.repository;



import ru.practicum.common.dto.events.EventFullDto;
import ru.practicum.common.dto.events.EventShortDto;
import ru.practicum.eventsService.event.dto.paramDto.EventRepositoryParam;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface EventRepositoryCustom {

    List<EventShortDto> findEventsShortDto(EventRepositoryParam param);

    Optional<EventFullDto> findEventByIdFullDto(Long id);

    List<EventFullDto> findEventsFullDto(EventRepositoryParam param);

    Map<Long, Integer> findParticipantLimitsByIdIn(List<Long> eventIds);
}
