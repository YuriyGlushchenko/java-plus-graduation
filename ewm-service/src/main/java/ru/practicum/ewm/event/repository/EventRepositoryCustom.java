package ru.practicum.ewm.event.repository;

import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.paramDto.EventRepositoryParam;

import java.util.List;
import java.util.Optional;

public interface EventRepositoryCustom {

    List<EventShortDto> findEventsShortDto(EventRepositoryParam param);

    Optional<EventFullDto> findEventByIdFullDto(Long id);

    List<EventFullDto> findEventsFullDto(EventRepositoryParam param);
}
