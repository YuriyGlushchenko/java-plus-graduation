package ru.practicum.requestsService.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.common.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.common.dto.participationRequest.RequestStatus;


import ru.practicum.common.exceptions.exceptions.ConditionsNotMetException;
import ru.practicum.common.exceptions.exceptions.NotFoundException;
import ru.practicum.requestsService.request.dto.ParticipationRequestMapper;
import ru.practicum.requestsService.request.model.ParticipationRequest;
import ru.practicum.requestsService.request.repository.ParticipationRequestRepository;
import ru.practicum.userService.user.model.User;
import ru.practicum.userService.user.repository.UserRepository;


import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipationRequestServiceImpl implements ParticipationRequestService {
//    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;
    private final UserRepository userRepository;


    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        // DataIntegrityViolationException c 409 кодом и так будет при нарушении уникальности в БД, но можно и явно проверить
        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConditionsNotMetException("Request already exists for this event");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        // toDo меняем на вызов через Feign Client в микросервис событий

//        Event event = eventRepository.findById(eventId)
//                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
//
//        if (event.getInitiator().getId().equals(userId)) {
//            throw new ConditionsNotMetException("Initiator can`t add request to his own event");
//        }
//
//        if (!event.getState().equals(EventState.PUBLISHED)) {
//            throw new ConditionsNotMetException("Impossible to add request to not published event");
//        }
//
//        if (event.getParticipantLimit() != 0) {
//            long participants = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
//            if (participants >= event.getParticipantLimit()) {
//                throw new ConditionsNotMetException("The limit of participation requests has been reached: " + participants);
//            }
//        }
//
//        ParticipationRequest request = ParticipationRequestMapper.toParticipationRequest(event, user);
//        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
//            request.setStatus(RequestStatus.CONFIRMED);
//        }
//        request = requestRepository.save(request);

//        return ParticipationRequestMapper.toParticipationRequestDto(request);

        return null;
    }

    @Override
    public List<ParticipationRequestDto> getUserParticipationRequests(Long userId) {
        List<ParticipationRequest> requests = requestRepository.findByRequesterId(userId);

        if (requests.isEmpty()) {
            return List.of();
        }

        return ParticipationRequestMapper.toParticipationRequestDto(requests);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {

        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Request with id=" + requestId + " not found for user with id=" + userId);
        }

        // отменить можно только PENDING и CONFIRMED? в ТЗ явно не указано
        if (request.getStatus() != RequestStatus.PENDING && request.getStatus() != RequestStatus.CONFIRMED) {
            throw new ConditionsNotMetException("Only requests with PENDING or CONFIRMED status can be canceled. Current status: " + request.getStatus());
        }

        request.setStatus(RequestStatus.CANCELED);

        request = requestRepository.save(request);

        return ParticipationRequestMapper.toParticipationRequestDto(request);
    }
}
