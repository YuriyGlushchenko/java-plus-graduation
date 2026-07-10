package ru.practicum.analyzer.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.model.Interaction;
import ru.practicum.analyzer.model.RecommendedEventDto;
import ru.practicum.analyzer.model.Similarity;
import ru.practicum.analyzer.repository.InteractionRepository;
import ru.practicum.analyzer.repository.SimilarityRepository;
import ru.practicum.analyzer.repository.projection.NeighborProjection;
import ru.practicum.analyzer.repository.projection.RecommendedEventProjection;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final InteractionRepository interactionRepository;
    private final SimilarityRepository similarityRepository;

    private static final int N_MAX_RECENT_INTERACTIONS = 10;
    private static final int LIMIT_SIMILARITY = 100;
    private static final int K_NEIGHBORS = 5;

    // ==================== ГЛАВНЫЙ МЕТОД ====================

    @Transactional(readOnly = true)
    @Override
    public List<RecommendedEventDto> getRecommendationsForUser(Long userId, int maxResults) {
        log.debug("Getting recommendations for user {}", userId);

        // 1. Получаем последние взаимодействия пользователя
        List<Interaction> recentInteractions = interactionRepository.findTopNByUserId(
                userId,
                PageRequest.of(0, N_MAX_RECENT_INTERACTIONS)
        );

        if (recentInteractions.isEmpty()) {
            log.debug("User {} has no interactions", userId);
            return List.of();
        }

        // Все взаимодействия пользователя (для расчета рейтингов)
        List<Interaction> allInteractions = interactionRepository.findAllByUserId(userId);

        // ID последних мероприятий (для поиска кандидатов)
        List<Long> recentEventIds = recentInteractions.stream()
                .map(Interaction::getEventId)
                .toList();

        // Карта оценок пользователя
        Map<Long, Double> userRatings = allInteractions.stream()
                .collect(Collectors.toMap(
                        Interaction::getEventId,
                        Interaction::getWeight
                ));

        // Все просмотренные мероприятия
        List<Long> userEventIds = allInteractions.stream()
                .map(Interaction::getEventId)
                .toList();

        // ЭТАП 1
        List<RecommendedEventProjection> candidates =
                findRecommendedEvents(recentEventIds, userId);

        if (candidates.isEmpty()) {
            return List.of();
        }

        // ЭТАП 2
        List<RecommendedEventDto> recommendations =
                predictScores(candidates, userRatings, userEventIds);

        return recommendations.stream()
                .sorted(Comparator.comparing(RecommendedEventDto::getScore).reversed())
                .limit(maxResults)
                .toList();
    }

    // ==================== ЭТАП 1 ====================

    private List<RecommendedEventProjection> findRecommendedEvents(List<Long> recentEventIds,
                                                                   Long userId) {

        return similarityRepository.findRecommendedEvents(
                recentEventIds,
                userId,
                LIMIT_SIMILARITY
        );
    }

    // ==================== ЭТАП 2 ====================

    private List<RecommendedEventDto> predictScores(List<RecommendedEventProjection> candidates,
                                                    Map<Long, Double> userRatings,
                                                    List<Long> userEventIds) {

        return candidates.stream()
                .map(candidate -> predictScore(candidate, userRatings, userEventIds))
                .toList();
    }

    private RecommendedEventDto predictScore(RecommendedEventProjection candidate,
                                             Map<Long, Double> userRatings,
                                             List<Long> userEventIds) {

        List<NeighborProjection> neighbors =
                similarityRepository.findNearestNeighbors(
                        candidate.getEventId(),
                        userEventIds,
                        K_NEIGHBORS
                );

        if (neighbors.isEmpty()) {
            return RecommendedEventDto.builder()
                    .eventId(candidate.getEventId())
                    .score(0.0)
                    .build();
        }

        double weightedSum = 0.0;
        double similaritySum = 0.0;

        for (NeighborProjection neighbor : neighbors) {

            Double rating = userRatings.get(neighbor.getEventId());

            if (rating == null) {
                continue;
            }

            weightedSum += rating * neighbor.getSimilarity();
            similaritySum += neighbor.getSimilarity();
        }

        double predictedScore =
                similaritySum == 0
                        ? 0
                        : weightedSum / similaritySum;

        return RecommendedEventDto.builder()
                .eventId(candidate.getEventId())
                .score(predictedScore)
                .build();
    }

    // ==================== 2. Похожие мероприятия ====================

    @Transactional(readOnly = true)
    @Override
    public List<RecommendedEventDto> getSimilarEvents(Long eventId, Long userId, int maxResults) {
        log.debug("Getting similar events for event: {}, user: {}", eventId, userId);

        // 1. Получить похожие мероприятия.
        List<Similarity> similarities = similarityRepository.findByEventId(eventId);

        // 2. Получаем ID мероприятий, с которыми пользователь уже взаимодействовал
        List<Long> interactedEvents = interactionRepository.findEventIdsByUserId(userId);
        Set<Long> interactedSet = new HashSet<>(interactedEvents);

        // 3. Исключаем просмотренные мероприятия и сортируем по убыванию, берем N первых
        return similarities.stream()
                .map(sim -> {
                    Long otherId = sim.getEvent1().equals(eventId) ? sim.getEvent2() : sim.getEvent1();
                    return Map.entry(otherId, sim.getSimilarity());
                })
                .filter(entry -> !interactedSet.contains(entry.getKey()))
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(entry -> RecommendedEventDto.builder()
                        .eventId(entry.getKey())
                        .score(entry.getValue())
                        .build())
                .collect(Collectors.toList());

    }

    // ==================== 3. Количество взаимодействий ====================

    @Transactional(readOnly = true)
    @Override
    public List<RecommendedEventDto> getInteractionsCount(List<Long> eventIds) {
        log.debug("Getting interactions count for {} events", eventIds.size());

        Map<Long, Double> eventWeights = new HashMap<>();

        for (Long eventId : eventIds) {
            // Суммируем все взаимодействия для мероприятия
            List<Interaction> interactions = interactionRepository.findAllByEventId(eventId);
            double totalWeight = interactions.stream()
                    .mapToDouble(Interaction::getWeight)
                    .sum();

            eventWeights.put(eventId, totalWeight);
        }

        return eventWeights.entrySet().stream()
                .map(entry -> RecommendedEventDto.builder()
                        .eventId(entry.getKey())
                        .score(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }
}