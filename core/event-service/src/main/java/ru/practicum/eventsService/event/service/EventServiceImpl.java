package ru.practicum.eventsService.event.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.dto.comments.CommentStatus;
import ru.practicum.common.dto.events.*;
import ru.practicum.common.dto.participationRequest.EventRequestStatusUpdateRequest;
import ru.practicum.common.dto.participationRequest.EventRequestStatusUpdateResult;
import ru.practicum.common.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.common.dto.users.UserShortDto;
import ru.practicum.common.exceptions.exceptions.ConditionsNotMetException;
import ru.practicum.common.exceptions.exceptions.NotFoundException;
import ru.practicum.eventsService.categories.model.Category;
import ru.practicum.eventsService.categories.repository.CategoryRepository;
import ru.practicum.eventsService.client.CommentClient;
import ru.practicum.eventsService.client.ParticipationRequestClient;
import ru.practicum.eventsService.client.UserClient;
import ru.practicum.eventsService.event.dto.EventMapper;
import ru.practicum.eventsService.event.dto.NewEventDto;
import ru.practicum.eventsService.event.dto.UpdateEventAdminRequest;
import ru.practicum.eventsService.event.dto.UpdateEventUserRequest;
import ru.practicum.eventsService.event.dto.paramDto.AdminUserEventParam;
import ru.practicum.eventsService.event.dto.paramDto.EventRepositoryParam;
import ru.practicum.eventsService.event.dto.paramDto.PublicUserEventParam;
import ru.practicum.eventsService.event.model.AdminStateAction;
import ru.practicum.eventsService.event.model.Event;
import ru.practicum.eventsService.event.model.EventSort;
import ru.practicum.eventsService.event.model.UserStateAction;
import ru.practicum.eventsService.event.repository.EventRepository;
import ru.practicum.stat.client.StatsClient;
import ru.practicum.stat.dto.EndpointHitDto;
import ru.practicum.stat.dto.ParamDto;
import ru.practicum.stat.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final StatsClient statsClient;
    private final UserClient userClient;
    private final ParticipationRequestClient requestClient;
    private final CommentClient commentClient;

    @Transactional
    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        validateEventDate(newEventDto.getEventDate(), 2);

        UserShortDto userDto = getUserById(userId);
        Category cat = categoryRepository.getCategory(newEventDto.getCategory());

        Event event = EventMapper.toEvent(newEventDto, cat, userId);
        event = eventRepository.save(event);

        return EventMapper.toEventFullDto(event, 0L, 0L, userDto);
    }

    /**
     * Возвращает список событий, созданных текущим пользователем.
     */
    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        EventRepositoryParam param = EventRepositoryParam.builder()
                .users(List.of(userId))
                .from(from)
                .size(size)
                .build();

        List<EventShortDto> events = eventRepository.findEventsShortDto(param);
        if (events.isEmpty()) {
            return events;
        }

        enrichEvents(events);
        return events;
    }

    /**
     * Возвращает список событий с фильтрацией. (Публичный запрос)
     */
    @Override
    public List<EventShortDto> getEventsForPublicRequests(PublicUserEventParam userEventParam) {
        EventRepositoryParam param = EventRepositoryParam.fromUserEventParam(userEventParam);

        List<EventShortDto> events = eventRepository.findEventsShortDto(param);
        if (events.isEmpty()) {
            return events;
        }

        // Фильтр onlyAvailable теперь обрабатывается тут, а не сразу в запросе в репозитории из-за разделения модулей
        if (param.isOnlyAvailable()) {
            events = filterAvailableEvents(events);
        }

        enrichEvents(events);

        if (param.getSortOrDefault() == EventSort.VIEWS) {  // из репозитория приходят уже отсортированными по дате
            events.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        }

        sendHit(userEventParam.getUri(), userEventParam.getIp(), LocalDateTime.now());

        return events;
    }

    /**
     * Возвращает список событий с фильтрацией. (Запрос администратора)
     */
    @Override
    public List<EventFullDto> getEventsForAdminRequests(AdminUserEventParam adminParam) {
        EventRepositoryParam param = EventRepositoryParam.fromAdminEventParam(adminParam);

        List<EventFullDto> events = eventRepository.findEventsFullDto(param);
        if (events.isEmpty()) {
            return events;
        }

        enrichEvents(events);
        return events;
    }

    /**
     * Возвращает полную информацию о событии, созданном текущим пользователем, по ID события.
     */
    @Override
    public EventFullDto findUserEventByEventId(Long userId, Long eventId) {
        EventFullDto event = findEventFullDtoById(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " not found for user with id=" + userId);
        }

        enrichEvent(event);
        return event;
    }

    /**
     * Обновляет событие, созданное текущим пользователем.
     */
    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest body) {
        Event event = getEventById(eventId);

        if (!event.getInitiatorId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " not found for user with id=" + userId);
        }

        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConditionsNotMetException("Only events with CANCELED or PENDING state can be updated");
        }

        validateEventDateForUpdate(event, body.getEventDate());

        if (body.getStateAction() != null) {
            updateEventState(event, body.getStateAction());
        }

        Category cat = null;
        if (body.getCategory() != null) {
            cat = categoryRepository.getCategory(body.getCategory());
        }
        EventMapper.updateEventFromUserRequest(body, event, cat);

        event = eventRepository.save(event);

        return buildEventFullDto(event);
    }

    /**
     * Обновляет любое событие (запрос администратора).
     */
    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest body) {
        Event event = getEventById(eventId);

        Category category = null;
        if (body.getCategory() != null) {
            category = categoryRepository.getCategory(body.getCategory());
        }

        EventMapper.updateEventFromAdminRequest(body, event, category);

        if (body.getStateAction() != null) {
            processAdminEventAction(event, body.getStateAction());
        }

        event = eventRepository.save(event);

        return buildEventFullDto(event);
    }

    /**
     * Возвращает опубликованное событие по его идентификатору.(Публичный запрос)
     */
    @Override
    public EventFullDto findEventById(String uri, String ip, Long id) {
        EventFullDto event = findEventFullDtoById(id);

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Published Event with id=" + id + " was not found");
        }

        sendHit(uri, ip, LocalDateTime.now());
        enrichEvent(event);

        return event;
    }

    /**
     * Возвращает список заявок на участие в событии, созданном текущим пользователем.
     */
    @Override
    public List<ParticipationRequestDto> getParticipationRequests(Long userId, Long eventId) {
        Event event = getEventById(eventId);

        if (!event.getInitiatorId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " not found for user with id=" + userId);
        }

        try {
            return requestClient.getRequestsByEventId(eventId);
        } catch (FeignException e) {
            log.error("Failed to get requests for eventId={}, status={}", eventId, e.status());
            throw new RuntimeException("Request service is currently unavailable", e);
        }
    }

    /**
     * Обновляет статусы заявок на участие в событии.
     */
    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatuses(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest) {
        Event event = getEventById(eventId);

        if (!event.getInitiatorId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " not found for user with id=" + userId);
        }

        try {
            return requestClient.updateRequestStatuses(eventId, updateRequest);
        } catch (FeignException e) {
            if (e.status() == 409) {
                log.warn("Request service conflict for eventId={}, body={}", eventId, e.contentUTF8());
                throw new ConditionsNotMetException("The participant limit has been reached or request status is not PENDING");
            } else {
                log.error("Request service unavailable for eventId={}, status={}, body={}",
                        eventId, e.status(), e.contentUTF8());
                throw new RuntimeException("Request service is currently unavailable", e);
            }
        }
    }

    /**
     * Возвращает список кратких DTO событий по их идентификаторам.
     */
    @Override
    public List<EventShortDto> getShortDtosByIds(Collection<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Event> events = eventRepository.findAllByIdIn(eventIds);

        List<EventShortDto> dtos = events.stream()
                .map(event -> EventMapper.toEventShortDto(event, 0L, 0L))
                .collect(Collectors.toList());

        enrichEvents(dtos);
        return dtos;
    }

    /**
     * Ищет в репозитории и возвращает событие по его идентификатору.
     */
    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private EventFullDto findEventFullDtoById(Long eventId) {
        return eventRepository.findEventByIdFullDto(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    /**
     * Запрашивает через Feign-клиент и возвращает краткую информацию о пользователе по его идентификатору.
     */
    private UserShortDto getUserById(Long userId) {
        try {
            UserShortDto user = userClient.getUserShortById(userId);
            if (user == null) {
                throw new NotFoundException("User with id=" + userId + " not found");
            }
            return user;
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new NotFoundException("User with id=" + userId + " was not found");
            } else {
                log.error("User service unavailable: status={}, error={}", e.status(), e.getMessage());
                throw new RuntimeException("User service is currently unavailable", e);
            }
        }
    }

    /**
     * Возвращает информацию в виде Map<userID, UserShortDto> обо всех пользователях, указанных в событиях из списка
       в качестве инициаторов или модераторов
     */
    private Map<Long, UserShortDto> getUsersDataMap(List<? extends Enrichable> events) {
        if (events.isEmpty()) {
            return Map.of();
        }

        Set<Long> userIds = events.stream()
                .map(Requestable::getId)
                .collect(Collectors.toSet());

        try {
            Map<Long, UserShortDto> userMap = userClient.getUsersDataByIds(new ArrayList<>(userIds));
            if (userMap == null) {
                log.warn("User service returned null for userIds: {}", userIds);
                return Map.of();
            }
            return userMap;
        } catch (FeignException e) {
            log.error("Failed to fetch users from user service: status={}, error={}", e.status(), e.getMessage());
            throw new RuntimeException("User service is currently unavailable", e);
        }
    }

    /**
     * Проверяет, что дата события не раньше указанного количества часов от текущего момента.
     */
    private void validateEventDate(LocalDateTime eventDate, int hours) {
        // "дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента"
        LocalDateTime minDate = LocalDateTime.now().plusHours(hours);
        if (eventDate.isBefore(minDate)) {
            throw new ConditionsNotMetException("Event date must be at least " + hours + " hours from now");
        }
    }

    /**
     * Проверяет дату события при обновлении.
     */
    private void validateEventDateForUpdate(Event event, LocalDateTime newEventDate) {
        LocalDateTime minEventDateForUpdating = LocalDateTime.now().plusHours(2);
        if (event.getEventDate().isBefore(minEventDateForUpdating)) {
            throw new ConditionsNotMetException("Unable to update event at last 2 hours before event date");
        }

        if (newEventDate != null && newEventDate.isBefore(minEventDateForUpdating)) {
            throw new ConditionsNotMetException("Unable to update event at last 2 hours before event date");
        }
    }

    /**
     * Обновляет статус события по действию пользователя.
     */
    private void updateEventState(Event event, UserStateAction action) {
        switch (action) {
            case SEND_TO_REVIEW:
                if (event.getState() == EventState.CANCELED) {
                    event.setState(EventState.PENDING);
                }
                break;
            case CANCEL_REVIEW:
                if (event.getState() != EventState.PENDING) {
                    throw new ConditionsNotMetException("Only events in PENDING state can be cancelled");
                }
                event.setState(EventState.CANCELED);
                break;
            default:
                throw new ConditionsNotMetException("Unknown state action: " + action);
        }
    }

    /**
     * Обрабатывает действие администратора над событием.
     */
    private void processAdminEventAction(Event event, AdminStateAction action) {
        switch (action) {
            case PUBLISH_EVENT:
                // "событие можно публиковать, только если оно в состоянии ожидания публикации
                if (event.getState() != EventState.PENDING) {
                    throw new ConditionsNotMetException("Cannot publish the event because it's not in the right state: " + event.getState());
                }
                // "дата начала изменяемого события должна быть не ранее чем за час от даты публикации. (Ожидается код ошибки 409)"
                // т.е. если собираемся опубликовать событие, то должен быть запас в час по времени
                validateEventDate(event.getEventDate(), 1);
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
                break;

            case REJECT_EVENT:
                // "событие можно отклонить, только если оно еще не опубликовано (Ожидается код ошибки 409)"
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConditionsNotMetException("Cannot reject published event");
                }
                event.setState(EventState.CANCELED);
                break;

            default:
                throw new ConditionsNotMetException("Unknown state action: " + action);
        }
    }

    /**
     * Фильтрует список событий, оставляя только доступные (где есть свободные места).
     */
    private List<EventShortDto> filterAvailableEvents(List<EventShortDto> events) {
        List<Long> eventIds = events.stream()
                .map(EventShortDto::getId)
                .collect(Collectors.toList());

        Map<Long, Integer> limits = eventRepository.findParticipantLimitsByIdIn(eventIds);
        Map<Long, Long> confirmedCounts;

        try {
            confirmedCounts = requestClient.getConfirmedRequestsCounts(eventIds);
        } catch (FeignException e) {
            log.error("Failed to get confirmed requests counts for eventIds={}, status={}", eventIds, e.status());
            throw new RuntimeException("Request service is currently unavailable", e);
        }

        return events.stream()
                .filter(event -> {
                    Integer limit = limits.getOrDefault(event.getId(), 0);
                    if (limit == 0) {
                        return true;
                    }
                    Long confirmed = confirmedCounts.getOrDefault(event.getId(), 0L);
                    return confirmed < limit;
                })
                .collect(Collectors.toList());
    }

    /**
     * Дополняет каждое событие из списка информацией о подтвержденных заявках, просмотрах и количестве комментариев.
     */
    private void enrichEvents(List<? extends Enrichable> events) {
        if (events.isEmpty()) {
            return;
        }
        enrichEventsWithConfirmedRequests(events);
        enrichEventsWithViews(events);
        enrichEventsListWithCommentsCount(events);
        enrichEventsWithUsers(events);
    }

    private void enrichEvent(EventFullDto event) {
        enrichEvents(List.of(event));
    }

    /**
     * Дополняет каждое событие из списка количеством подтвержденных заявок.
     */
    private void enrichEventsWithConfirmedRequests(List<? extends Requestable> events) {
        if (events.isEmpty()) {
            return;
        }

        List<Long> eventIds = events.stream()
                .map(Requestable::getId)
                .collect(Collectors.toList());

        try {
            Map<Long, Long> counts = requestClient.getConfirmedRequestsCounts(eventIds);
            events.forEach(event ->
                    event.setConfirmedRequests(counts.getOrDefault(event.getId(), 0L))
            );
        } catch (FeignException e) {
            log.error("Failed to get confirmed requests counts for eventIds={}, status={}", eventIds, e.status());
            events.forEach(event -> event.setConfirmedRequests(0L));
        }
    }

    /**
     * Дополняет каждое событие из списка  количеством просмотров.
     */
    private void enrichEventsWithViews(List<? extends Viewable> events) {
        if (events.isEmpty()) {
            return;
        }

        LocalDateTime minEventDate = events.stream()
                .map(Viewable::getPublishedOn)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        String[] uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .toArray(String[]::new);

        Map<Long, Long> hits = fetchViews(uris, minEventDate);

        events.forEach(event ->
                event.setViews(hits.getOrDefault(event.getId(), 0L))
        );
    }

    /**
     * Дополняет каждое событие из списка  количеством комментариев.
     */
    private void enrichEventsListWithCommentsCount(List<? extends Commentable> eventDtos) {
        if (eventDtos.isEmpty()) {
            return;
        }

        List<Long> ids = eventDtos.stream()
                .map(Commentable::getId)
                .collect(Collectors.toList());

        try {
            Map<Long, Long> countsMap = commentClient.getCommentCountsByEventIds(ids, CommentStatus.APPROVED);
            eventDtos.forEach(item -> item.setCommentsCount(countsMap.getOrDefault(item.getId(), 0L)));
        } catch (FeignException e) {
            log.error("Failed to get comments counts for eventIds={}, status={}", ids, e.status());
            eventDtos.forEach(item -> item.setCommentsCount(0L));
        }
    }

    /**
     * Дополняет каждое событие из списка данными пользователей (инициаторов).
     */
    private void enrichEventsWithUsers(List<? extends Enrichable> events) {
        if (events.isEmpty()) {
            return;
        }

        Map<Long, UserShortDto> userMap = getUsersDataMap(events);
        if (userMap.isEmpty()) {
            return;
        }

        events.forEach(event -> {
            UserShortDto currentInitiator = event.getInitiator();

            if (currentInitiator == null || currentInitiator.getId() == null) {
                log.debug("Event {} has no initiator", event.getId());
                return;
            }

            UserShortDto enrichedUser = userMap.get(currentInitiator.getId());
            if (enrichedUser != null) {
                event.setInitiator(enrichedUser);
            } else {
                log.warn("User not found while enriching event for userId: {}", currentInitiator);
                currentInitiator.setName("Unknown");
            }
        });
    }

    /**
     * Собирает полное DTO события с полными данными по просмотрам, заявкам, комментариям.
     */
    private EventFullDto buildEventFullDto(Event event) {
        String[] uris = {"/events/" + event.getId()};
        Map<Long, Long> hits = fetchViews(uris, event.getEventDate());
        Long views = hits.getOrDefault(event.getId(), 0L);

        Long confirmedRequests = getConfirmedRequestsCount(event.getId());
        UserShortDto initiator = getUserById(event.getInitiatorId());

        EventFullDto eventFullDto = EventMapper.toEventFullDto(event, confirmedRequests, views, initiator);
        enrichEventsListWithCommentsCount(List.of(eventFullDto));

        return eventFullDto;
    }

    /**
     * Запрашивает в сервисе заявок через feign-клиент и возвращает количество подтвержденных заявок на событие.
     */
    private Long getConfirmedRequestsCount(Long eventId) {
        try {
            return requestClient.getConfirmedRequestsCount(eventId);
        } catch (FeignException e) {
            log.error("Failed to get confirmed requests count for eventId={}, status={}", eventId, e.status());
            return 0L;
        }
    }

    /**
     * Запрашивает в сервисе статистики через feign-клиент и возвращает количество просмотров событий.
     */
    private Map<Long, Long> fetchViews(String[] uris, LocalDateTime date) {
        ParamDto statRequestParam = ParamDto.builder()
                .start(date)
                .end(LocalDateTime.now().plusSeconds(1))
                .uris(uris)
                .unique(true)
                .build();

        log.debug("Fetching views for uris: {}, params: {}", Arrays.toString(uris), statRequestParam);

        try {
            List<ViewStatsDto> stats = statsClient.get(statRequestParam);
            log.debug("Stats received from client: {}", stats);

            if (stats.size() == 1 && stats.getFirst().getHits() == -1) {
                log.error("Failed to fetch views from stats-service, returned hits = -1 (Fail marker)");
                return Collections.emptyMap();
            }

            return stats.stream()
                    .filter(stat -> stat.getUri() != null && stat.getHits() != -1)
                    .collect(Collectors.toMap(
                            this::extractEventIdFromUri,
                            ViewStatsDto::getHits
                    ));
        } catch (Exception e) {
            log.error("Failed to fetch views from stats-service", e);
            return Collections.emptyMap();
        }
    }

    /**
     * Извлекает идентификатор события (Id) из URI.
     */
    private Long extractEventIdFromUri(ViewStatsDto stat) {
        String uri = stat.getUri(); // приходить должно в формате "/events/{id}"
        return Long.parseLong(uri.substring(uri.lastIndexOf('/') + 1));
    }

    /**
     * Отправляет информацию о просмотре события в сервис статистики.
     */
    private void sendHit(String uri, String ip, LocalDateTime time) {
        EndpointHitDto hitDto = EndpointHitDto.builder()
                .uri(uri)
                .ip(ip)
                .timestamp(time)
                .build();
        statsClient.hit(hitDto);
    }

}