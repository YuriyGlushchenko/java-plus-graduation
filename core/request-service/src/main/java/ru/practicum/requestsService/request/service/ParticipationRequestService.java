package ru.practicum.requestsService.request.service;


import ru.practicum.common.dto.participationRequest.EventRequestStatusUpdateRequest;
import ru.practicum.common.dto.participationRequest.EventRequestStatusUpdateResult;
import ru.practicum.common.dto.participationRequest.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

public interface ParticipationRequestService {

    ParticipationRequestDto createRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> getUserParticipationRequests(Long userId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    /**
     * Получить количество подтвержденных заявок по событию
     */
    long getConfirmedRequestsCount(Long eventId);

    /**
     * Получить все заявки по событию
     */
    List<ParticipationRequestDto> getRequestsByEventId(Long eventId);

    /**
     * Изменить статусы заявок
     */
    EventRequestStatusUpdateResult updateRequestStatuses(Long eventId, int limit, EventRequestStatusUpdateRequest request);

    /**
     * Получить количество подтвержденных заявок по списку событий
     */
    Map<Long, Long> getConfirmedRequestsCounts(List<Long> eventIds);

    boolean hasConfirmedParticipation(Long eventId, Long userId);


}
