package ru.practicum.analyzer.repository.projection;

public interface RecommendedEventProjection {

    Long getEventId();

    Double getSimilarity();
}
