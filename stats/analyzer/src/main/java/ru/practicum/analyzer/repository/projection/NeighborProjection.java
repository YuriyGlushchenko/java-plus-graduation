package ru.practicum.analyzer.repository.projection;

public interface NeighborProjection {
    Long getCandidateEventId();

    Long getEventId();

    Double getSimilarity();

    Double getUserRating();

}