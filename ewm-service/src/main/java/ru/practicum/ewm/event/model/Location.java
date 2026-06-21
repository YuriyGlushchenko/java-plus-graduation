package ru.practicum.ewm.event.model;

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
public class Location {
    @Column(name = "lat")
    private Float lat;  // широта

    @Column(name = "lon")
    private Float lon;  // долгота
}