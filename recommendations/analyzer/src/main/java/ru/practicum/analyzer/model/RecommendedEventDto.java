package ru.practicum.analyzer.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecommendedEventDto {
    private Long eventId;
    private Double score;
}