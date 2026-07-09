package ru.practicum.analyzer.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.model.RecommendedEventDto;

import java.util.List;

public interface RecommendationService {
    @Transactional(readOnly = true)
    List<RecommendedEventDto> getRecommendationsForUser(Long userId, int maxResults);

    @Transactional(readOnly = true)
    List<RecommendedEventDto> getSimilarEvents(Long eventId, Long userId, int maxResults);

    @Transactional(readOnly = true)
    List<RecommendedEventDto> getInteractionsCount(List<Long> eventIds);
}
