package ru.practicum.ewm.event.dto.paramDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.model.EventSort;
import ru.practicum.ewm.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;


// класс для передачи параметров для отбора событий в репозиторий
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRepositoryParam {
    private String text;
    private List<Long> categories;
    private List<Long> users;
    private List<EventState> states;
    private Boolean paid;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Boolean onlyAvailable;
    private EventSort sort;
    private Integer from;
    private Integer size;
    private boolean publicRequest; // указывает на запрос с публичного эндпоинта, тогда нужны только PUBLISHED события
    private List<Long> ids;

    public static EventRepositoryParam fromUserEventParam(PublicUserEventParam userParam) {
        return EventRepositoryParam.builder()
                .text(userParam.getText())
                .categories(userParam.getCategories())
                .paid(userParam.getPaid())
                .rangeStart(userParam.getRangeStart())
                .rangeEnd(userParam.getRangeEnd())
                .onlyAvailable(userParam.getOnlyAvailable())
                .sort(userParam.getSort())
                .from(userParam.getFrom())
                .size(userParam.getSize())
                .publicRequest(true)
                .build();
    }

    public static EventRepositoryParam fromAdminEventParam(AdminUserEventParam adminParam) {
        return EventRepositoryParam.builder()
                .users(adminParam.getUsers())
                .states(adminParam.getStates())
                .categories(adminParam.getCategories())
                .rangeStart(adminParam.getRangeStart())
                .rangeEnd(adminParam.getRangeEnd())
                .from(adminParam.getFrom())
                .size(adminParam.getSize())
                .build();
    }

    public boolean hasIds() {
        return ids != null && !ids.isEmpty();
    }

    public boolean isPublicRequest() {
        return publicRequest;
    }

    public boolean hasUsers() {
        return users != null && !users.isEmpty();
    }

    public boolean hasStates() {
        return states != null && !states.isEmpty();
    }

    public boolean hasPaidParam() {
        return paid != null;
    }

    public boolean isOnlyAvailable() {
        return onlyAvailable != null ? onlyAvailable : false;
    }

    public EventSort getSortOrDefault() {
        return sort != null ? sort : EventSort.EVENT_DATE;
    }

    public boolean hasDateRange() {
        return rangeStart != null && rangeEnd != null;
    }

    public boolean hasRangeStart() {
        return rangeStart != null;
    }

    public boolean hasRangeEnd() {
        return rangeEnd != null;
    }

    public boolean hasTextSearchRequest() {
        return text != null && !text.isBlank();
    }

    public boolean hasCategories() {
        return categories != null && !categories.isEmpty();
    }
}
