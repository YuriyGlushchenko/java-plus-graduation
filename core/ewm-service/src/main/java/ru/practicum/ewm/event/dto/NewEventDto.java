package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.model.Location;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {
    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;

    @NotNull
    private Long category;  // id категории

    @NotBlank
    @Size(min = 20, max = 7000)
    private String description;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Future
    private LocalDateTime eventDate;  // "yyyy-MM-dd HH:mm:ss"

    @NotNull
    private Location location;

    private Boolean paid;  // по умолчанию false

    @Min(0)
    private Integer participantLimit;  // по умолчанию 0

    private Boolean requestModeration;  // по умолчанию true

    @NotBlank
    @Size(min = 3, max = 120)
    private String title;
}
