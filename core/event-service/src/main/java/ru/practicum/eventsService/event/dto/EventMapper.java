package ru.practicum.eventsService.event.dto;

import lombok.RequiredArgsConstructor;
import ru.practicum.common.dto.events.*;
import ru.practicum.common.dto.users.UserShortDto;
import ru.practicum.eventsService.categories.dto.CategoryMapper;
import ru.practicum.eventsService.categories.model.Category;
import ru.practicum.eventsService.event.model.Event;
import ru.practicum.eventsService.event.model.EventLocation;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class EventMapper {

    public static Event toEvent(NewEventDto newEventDto, Category category, Long initiatorId) {
        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .category(category)
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .location(toEventLocation(newEventDto.getLocation()))
                .paid(newEventDto.getPaid() != null ? newEventDto.getPaid() : false)
                .participantLimit(newEventDto.getParticipantLimit() != null ? newEventDto.getParticipantLimit() : 0)
                .requestModeration(newEventDto.getRequestModeration() != null ? newEventDto.getRequestModeration() : true)
                .title(newEventDto.getTitle())
                .initiatorId(initiatorId)
                .state(EventState.PENDING)
                .createdOn(LocalDateTime.now())
                .build();
    }

    public static void updateEventFromAdminRequest(UpdateEventAdminRequest request, Event event, Category category) {
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (category != null) {
            event.setCategory(category);
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }
        if (request.getLocation() != null) {
            event.setLocation(toEventLocation(request.getLocation()));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
    }

    public static void updateEventFromUserRequest(UpdateEventUserRequest request, Event event, Category category) {
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (category != null) {
            event.setCategory(category);
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }
        if (request.getLocation() != null) {
            event.setLocation(toEventLocation(request.getLocation()));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
    }

    public static EventFullDto toEventFullDto(Event event, Long confirmedRequests, Long views, UserShortDto initiator) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(initiator)
                .location(toLocation(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(views)
                .build();
    }

    public static EventShortDto toEventShortDto(Event event, Long confirmedRequests, Long views) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .eventDate(event.getEventDate())
                .initiator(
                        UserShortDto.builder()
                                .id(event.getInitiatorId())
                                .build()
                )
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(views)
                .build();
    }

    public static EventBaseDto toEventBaseDto(Event event) {
        if (event == null) {
            return null;
        }

        return EventBaseDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(
                        UserShortDto.builder()
                                .id(event.getInitiatorId())
                                .build()
                )
                .location(toLocation(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .build();
    }


    private static EventLocation toEventLocation(Location location) {
        if (location == null) {
            return null;
        }

        return EventLocation.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }


    private static Location toLocation(EventLocation location) {
        if (location == null) {
            return null;
        }

        return Location.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }
}