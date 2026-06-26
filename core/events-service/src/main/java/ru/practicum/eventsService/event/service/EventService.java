package ru.practicum.eventsService.event.service;



import ru.practicum.common.dto.participationRequest.EventRequestStatusUpdateRequest;
import ru.practicum.common.dto.participationRequest.EventRequestStatusUpdateResult;
import ru.practicum.common.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.eventsService.event.dto.*;
import ru.practicum.eventsService.event.dto.paramDto.AdminUserEventParam;
import ru.practicum.eventsService.event.dto.paramDto.PublicUserEventParam;

import java.util.Collection;
import java.util.List;

public interface EventService {

    List<EventShortDto> getEventsForPublicRequests(PublicUserEventParam param);

    List<EventFullDto> getEventsForAdminRequests(AdminUserEventParam param);

    EventFullDto findEventById(String uri, String ip, Long id);

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getUserEvents(Long userId, int from, int size);

    EventFullDto findUserEventByEventId(Long userId, Long eventId);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest body);

    EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest body);

    List<ParticipationRequestDto> getParticipationRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatuses(Long userId, Long eventId, EventRequestStatusUpdateRequest request);

    List<EventShortDto> getShortDtosByIds(Collection<Long> eventIds);


}
