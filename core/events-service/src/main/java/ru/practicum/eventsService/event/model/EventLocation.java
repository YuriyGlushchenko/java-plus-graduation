package ru.practicum.eventsService.event.model;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventLocation {

    @Column(name = "lat")
    private Float lat;

    @Column(name = "lon")
    private Float lon;
}