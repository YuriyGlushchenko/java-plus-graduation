package ru.practicum.requestsService.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.dto.participationRequest.EventRequestStatusUpdateRequest;
import ru.practicum.common.dto.participationRequest.EventRequestStatusUpdateResult;
import ru.practicum.common.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.common.dto.participationRequest.RequestStatus;


import ru.practicum.common.exceptions.exceptions.ConditionsNotMetException;
import ru.practicum.common.exceptions.exceptions.NotFoundException;
import ru.practicum.requestsService.request.dto.ParticipationRequestMapper;
import ru.practicum.requestsService.request.model.ParticipationRequest;
import ru.practicum.requestsService.request.repository.ParticipationRequestRepository;
import ru.practicum.userService.user.model.User;
import ru.practicum.userService.user.repository.UserRepository;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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












    @Override
    public long getConfirmedRequestsCount(Long eventId) {
        return requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByEventId(Long eventId) {
        List<ParticipationRequest> requests = requestRepository.findByEventId(eventId);
        return ParticipationRequestMapper.toParticipationRequestDto(requests);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatuses(Long eventId, EventRequestStatusUpdateRequest updateRequest) {
        // Проверяем, что заявки существуют
        List<ParticipationRequest> requests = requestRepository.findByIdIn(updateRequest.getRequestIds());

        if (requests.isEmpty()) {
            throw new NotFoundException("Requests not found for ids: " + updateRequest.getRequestIds());
        }

        // Проверяем, что все заявки относятся к данному событию
        for (ParticipationRequest r : requests) {
            if (!r.getEventId().equals(eventId)) {
                throw new ConditionsNotMetException("Request with id=" + r.getId() + " is not related to event=" + eventId);
            }
            if (!r.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConditionsNotMetException("Only PENDING requests can be reviewed");
            }
        }

        List<ParticipationRequest> approved = new ArrayList<>();
        List<ParticipationRequest> rejected = new ArrayList<>();

        // Получаем текущее количество подтвержденных заявок
        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        // Получаем лимит (для этого нужен вызов к events-service, но пока не реализовано)
        // Временно используем большой лимит, пока не реализуем
        long limit = 1000; // TODO: получить лимит из events-service

        if (updateRequest.getStatus().equals(RequestStatus.CONFIRMED)) {
            for (ParticipationRequest r : requests) {
                if (confirmedCount < limit) {
                    approved.add(r);
                    confirmedCount++;
                } else {
                    rejected.add(r);
                }
            }
        } else {
            rejected.addAll(requests);
        }

        // Обновляем статусы в БД
        updateStatuses(approved, RequestStatus.CONFIRMED);
        updateStatuses(rejected, RequestStatus.REJECTED);

        // Если лимит исчерпан, отклоняем все остальные PENDING заявки
        if (limit > 0 && confirmedCount >= limit) {
            requestRepository.updateStatusByEventId(eventId, RequestStatus.PENDING, RequestStatus.REJECTED);
        }

        return ParticipationRequestMapper.toEventRequestStatusUpdateResult(approved, rejected);
    }

    @Override
    public Map<Long, Long> getConfirmedRequestsCounts(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> counts = requestRepository.countByEventIdInAndStatus(eventIds, RequestStatus.CONFIRMED);
        return counts.stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Long) arr[1]
                ));
    }

    private void updateStatuses(List<ParticipationRequest> requests, RequestStatus status) {
        if (requests.isEmpty()) {
            return;
        }
        List<Long> ids = requests.stream()
                .map(ParticipationRequest::getId)
                .collect(Collectors.toList());
        requestRepository.updateStatusByIdIn(ids, status);
        requests.forEach(r -> r.setStatus(status));
    }
}
