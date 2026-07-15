package ru.practicum.analyzer.service;

import ru.practicum.analyzer.model.Similarity;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.List;

public interface SimilarityService {

    void saveSimilarity(EventSimilarityAvro eventSimilarityAvro);

    List<Similarity> getSimilaritiesForEvent(Long eventId);

    List<Similarity> getTopSimilarities(Long eventId, int limit);
}
