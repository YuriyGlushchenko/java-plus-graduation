package ru.practicum.eventsService.event.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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