package ru.practicum.ewm.comments.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentFullDto {
    private Long id;
    private String text;
    private UserShortDto author;
    private Long eventId;
    private LocalDateTime created;
    private LocalDateTime updated;
    private String status;
    private UserShortDto moderator;
    private LocalDateTime moderatedAt;
}
