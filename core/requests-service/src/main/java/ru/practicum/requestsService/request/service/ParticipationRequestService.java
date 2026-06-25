package ru.practicum.requestsService.request.service;



import ru.practicum.requestsService.request.dto.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {

    ParticipationRequestDto createRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> getUserParticipationRequests(Long userId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);


}
