package ru.practicum.aggregator.service;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface SimilarityService {
    void initSimilarityCalculation(UserActionAvro userActionAvro);
}
