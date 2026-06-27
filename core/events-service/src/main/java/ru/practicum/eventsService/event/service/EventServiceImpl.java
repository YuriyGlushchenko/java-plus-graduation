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
import ru.practicum.eventsService.client.CommentClint;
import ru.practicum.eventsService.client.ParticipationRequestClient;
import ru.practicum.eventsService.client.UserClient;
import ru.practicum.eventsService.event.dto.EventMapper;
import ru.practicum.eventsService.event.dto.NewEventDto;
import ru.practicum.eventsService.event.dto.UpdateEventAdminRequest;
import ru.practicum.eventsService.event.dto.UpdateEventUserRequest;
import ru.practicum.eventsService.event.dto.paramDto.AdminUserEventParam;
import ru.practicum.eventsService.event.dto.paramDto.EventRepositoryParam;
import ru.practicum.eventsService.event.dto.paramDto.PublicUserEventParam;
import ru.practicum.eventsService.event.model.Event;
import ru.practicum.eventsService.event.model.EventSort;
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
    private final CommentClint commentClint;

    @Transactional
    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        // "дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента"
        LocalDateTime minEventDate = LocalDateTime.now().plusHours(2);
        if (newEventDto.getEventDate().isBefore(minEventDate)) {
            throw new ConditionsNotMetException("Event date must be at least 2 hours from now");
        }

        // раз по условию пользователь аутентифицирован и авторизован, значит он точно есть
        UserShortDto userDto;

        try {
            userDto = userClient.getUserShortById(userId);
            if (userDto == null) {
                throw new NotFoundException("User with id=" + userId + " not found");
            }
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new NotFoundException("User with id=" + userId + " was not found");
            } else {
                log.error("User service unavailable: status={}, error={}", e.status(), e.getMessage());
                throw new RuntimeException("User service is currently unavailable", e);
            }
        }

        Category cat = categoryRepository.getCategory(newEventDto.getCategory());

        Event event = EventMapper.toEvent(newEventDto, cat, userId);

        event = eventRepository.save(event);

        return EventMapper.toEventFullDto(event, 0L, 0L, userDto);
    }

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

        // ✅ Получаем количество подтвержденных заявок через Feign Client
        enrichEventsWithConfirmedRequests(events);
        enrichEventsWithViews(events);
        enrichEventsListWithCommentsCount(events);

        return events;
    }

    @Override
    public List<EventShortDto> getEventsForPublicRequests(PublicUserEventParam userEventParam) {
        EventRepositoryParam param = EventRepositoryParam.fromUserEventParam(userEventParam);

        List<EventShortDto> events = eventRepository.findEventsShortDto(param);
        if (events.isEmpty()) {
            return events;
        }

        // Фильтр onlyAvailable теперь обрабатывается тут, а не в репозитории из-за разделения модулей
        if (param.isOnlyAvailable()) {
            List<Long> eventIds = events.stream().map(EventShortDto::getId).collect(Collectors.toList());

            // Получаем лимиты из репозитория
            Map<Long, Integer> limits = eventRepository.findParticipantLimitsByIdIn(eventIds);
            Map<Long, Long> confirmedCounts = requestClient.getConfirmedRequestsCounts(eventIds);

            events = events.stream()
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

        // ✅ Получаем количество подтвержденных заявок через Feign Client
        enrichEventsWithConfirmedRequests(events);
        enrichEventsWithViews(events);
        enrichEventsListWithCommentsCount(events);

        if (param.getSortOrDefault() == EventSort.VIEWS) {  // из репозитория приходят уже отсортированными по дате
            events.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        }

        sendHit(userEventParam.getUri(), userEventParam.getIp(), LocalDateTime.now());

        return events;
    }

    @Override
    public List<EventFullDto> getEventsForAdminRequests(AdminUserEventParam adminParam) {
        EventRepositoryParam param = EventRepositoryParam.fromAdminEventParam(adminParam);

        List<EventFullDto> events = eventRepository.findEventsFullDto(param);
        if (events.isEmpty()) {
            return events;
        }

        // ✅ Получаем количество подтвержденных заявок через Feign Client
        enrichEventsWithConfirmedRequests(events);
        enrichEventsWithViews(events);
        enrichEventsListWithCommentsCount(events);

        return events;
    }

    @Override
    public EventFullDto findUserEventByEventId(Long userId, Long eventId) {

        EventFullDto event = eventRepository.findEventByIdFullDto(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " not found for user with id=" + userId);
        }

        // ✅ Получаем количество подтвержденных заявок через Feign Client
        enrichEventWithConfirmedRequests(event);
        enrichEventWithViews(event);
        enrichEventsListWithCommentsCount(List.of(event));

        return event;
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest body) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getInitiatorId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " not found for user with id=" + userId);
        }

        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConditionsNotMetException("Only events with CANCELED or PENDING state can be updated");
        }

        // в спецификации не сказано четко, какую дату проверять, которая уже в событии или новую(если есть), проверим обе
        LocalDateTime minEventDateForUpdating = LocalDateTime.now().plusHours(2);
        if (event.getEventDate().isBefore(minEventDateForUpdating)) {
            throw new ConditionsNotMetException("Unable to update event at last 2 hours before event date");
        }

        if (body.getEventDate() != null) {
            LocalDateTime newEventDate = body.getEventDate();
            if (newEventDate.isBefore(minEventDateForUpdating)) {
                throw new ConditionsNotMetException("Unable to update event at last 2 hours before event date");
            }
        }

        if (body.getStateAction() != null) {
            switch (body.getStateAction()) {
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
                    throw new ConditionsNotMetException("Unknown state action: " + body.getStateAction());
            }
        }

        Category cat = null;
        if (body.getCategory() != null) {
            cat = categoryRepository.getCategory(body.getCategory());
        }
        EventMapper.updateEventFromUserRequest(body, event, cat);

        event = eventRepository.save(event);

        String[] uris = {"/events/" + event.getId()};
        Map<Long, Long> hits = fetchViews(uris, event.getEventDate());
        Long views = hits.getOrDefault(event.getId(), 0L);

        // ✅ Получаем количество подтвержденных заявок через Feign Client
        Long confirmedRequests = requestClient.getConfirmedRequestsCount(eventId);

        UserShortDto initiator = userClient.getUserShortById(event.getInitiatorId());

        EventFullDto eventFullDto = EventMapper.toEventFullDto(event, confirmedRequests, views, initiator);
        enrichEventsListWithCommentsCount(List.of(eventFullDto));

        return eventFullDto;
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest body) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        Category category = null;
        if (body.getCategory() != null) {
            category = categoryRepository.getCategory(body.getCategory());
        }

        EventMapper.updateEventFromAdminRequest(body, event, category);

        if (body.getStateAction() != null) {
            switch (body.getStateAction()) {
                case PUBLISH_EVENT:
                    // "событие можно публиковать, только если оно в состоянии ожидания публикации (Ожидается код ошибки 409)"
                    if (event.getState() != EventState.PENDING) {
                        String msg = "Cannot publish the event because it's not in the right state: " + event.getState();
                        throw new ConditionsNotMetException(msg);
                    }
                    // "дата начала изменяемого события должна быть не ранее чем за час от даты публикации. (Ожидается код ошибки 409)"
                    // т.е. если собираемся опубликовать событие, то должен быть запас в час по времени
                    LocalDateTime minPublishDate = LocalDateTime.now().plusHours(1);
                    if (event.getEventDate().isBefore(minPublishDate)) {
                        throw new ConditionsNotMetException("Event date must be at least 1 hour from now");
                    }
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
                    throw new ConditionsNotMetException("Unknown state action: " + body.getStateAction());
            }
        }

        event = eventRepository.save(event);

        String[] uris = {"/events/" + event.getId()};
        Map<Long, Long> hits = fetchViews(uris, event.getEventDate());
        Long views = hits.getOrDefault(event.getId(), 0L);

        Long confirmedRequests = requestClient.getConfirmedRequestsCount(eventId);

        UserShortDto initiator = userClient.getUserShortById(event.getInitiatorId());

        EventFullDto eventFullDto = EventMapper.toEventFullDto(event, confirmedRequests, views, initiator);
        enrichEventsListWithCommentsCount(List.of(eventFullDto));

        return eventFullDto;
    }

    public EventFullDto findEventById(String uri, String ip, Long id) {

        EventFullDto event = eventRepository.findEventByIdFullDto(id)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Published Event with id=" + id + " was not found");
        }

        sendHit(uri, ip, LocalDateTime.now());

        enrichEventWithConfirmedRequests(event);
        enrichEventWithViews(event);
        enrichEventsListWithCommentsCount(List.of(event));

        return event;
    }

    @Override
    public List<ParticipationRequestDto> getParticipationRequests(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

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

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatuses(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getInitiatorId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " not found for user with id=" + userId);
        }

        try {
            return requestClient.updateRequestStatuses(eventId, updateRequest);
        } catch (FeignException e) {
            log.error("Failed to update request statuses for eventId={}, status={}", eventId, e.status());
            throw new RuntimeException("Request service is currently unavailable", e);
        }
    }

    @Override
    public List<EventShortDto> getShortDtosByIds(Collection<Long> eventId) {
        if (eventId == null || eventId.isEmpty()) {
            return Collections.emptyList();
        }

        List<Event> events = eventRepository.findAllByIdIn(eventId);

        List<EventShortDto> dtos = events.stream()
                .map(event -> EventMapper.toEventShortDto(event, 0L, 0L))
                .collect(Collectors.toList());

        enrichEventsWithConfirmedRequests(dtos);
        enrichEventsWithViews(dtos);
        enrichEventsListWithCommentsCount(dtos);

        return dtos;
    }


    /**
     * Обогащает список событий реализующих Requestable (EventShortDto, EventFullDto) количеством подтвержденных заявок
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
     * Обогащает одно событие количеством подтвержденных заявок
     */
    private void enrichEventWithConfirmedRequests(EventFullDto event) {
        enrichEventsWithConfirmedRequests(List.of(event));
    }

    private void enrichEventsWithViews(List<? extends Viewable> events) {
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

    private void enrichEventWithViews(EventFullDto event) {
        enrichEventsWithViews(List.of(event));
    }

    private Long extractEventIdFromUri(ViewStatsDto stat) {
        String uri = stat.getUri(); // приходить должно в формате "/events/{id}"
        return Long.parseLong(uri.substring(uri.lastIndexOf('/') + 1));
    }

    private void sendHit(String uri, String ip, LocalDateTime time) {
        EndpointHitDto hitDto = EndpointHitDto.builder()
                .uri(uri)
                .ip(ip)
                .timestamp(time)
                .build();

        statsClient.hit(hitDto);
    }

    private void enrichEventsListWithCommentsCount(List<? extends Commentable> eventDtos) {
        if (eventDtos.isEmpty()) return;

        List<Long> ids = eventDtos.stream()
                .map(Commentable::getId)
                .collect(Collectors.toList());

        Map<Long, Long> countsMap = commentClint.getCommentCountsByEventIds(ids, CommentStatus.APPROVED);

        eventDtos.forEach(item -> item.setCommentsCount(countsMap.getOrDefault(item.getId(), 0L)));
    }
}