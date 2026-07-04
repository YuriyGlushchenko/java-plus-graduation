package ru.practicum.common.dto.comments;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.common.dto.users.UserShortDto;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // поля со значением null должны быть исключены из выходного JSON-документа
public class CommentShortDto {
    private Long id;
    private String text;
    private UserShortDto author;
    private Long eventId;
    private LocalDateTime created;
}
