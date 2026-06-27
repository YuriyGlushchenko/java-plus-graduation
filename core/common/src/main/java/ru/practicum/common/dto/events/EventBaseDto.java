package ru.practicum.common.dto.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.common.dto.events.category.CategoryDto;
import ru.practicum.common.dto.users.UserShortDto;

import java.time.LocalDateTime;

/**
 * Содержит полную базовую информацию о событии для внутреннего использования другими микросервисами.
 * Без подгрузки счётчики просмотров, подтверждённых заявок и комментариев и без подгрузки имени пользователя.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventBaseDto  {
    private Long id;

    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;

    private CategoryDto category;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;

    @NotBlank
    @Size(min = 20, max = 7000)
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private UserShortDto initiator;

    private Location location;

    private Boolean paid;

    private Integer participantLimit;  // по умолчанию 0

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;

    private Boolean requestModeration;  // по умолчанию true

    private EventState state;

    @NotBlank
    @Size(min = 3, max = 120)
    private String title;

}