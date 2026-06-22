package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.categories.dto.CategoryDto;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventShortDto implements Viewable, Commentable {
    private Long id;

    private String annotation;

    private CategoryDto category;

    private Long confirmedRequests;  // из запросов на участие

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private UserShortDto initiator;

    private Boolean paid;

    @JsonIgnore
    // подгрузим даты публикаций, для расчета минимальной даты для запроса статистики, но клиенту отправлять не будем
    private LocalDateTime publishedOn;

    private String title;

    private Long views;  // подгружаем отдельно из сервиса статистики

    private Long commentsCount;

}