package ru.practicum.common.dto.events;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    private Float lat;

    private Float lon;
}