package ru.practicum.analyzer.service;

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
    private static final int K_NEIGHBORS = 20;

    // ==================== предсказание оценки ===================

    @Transactional(readOnly = true)
    @Override
    public List<RecommendedEventDto> getRecommendationsForUser(Long userId, int maxResults) {
        log.debug("Getting recommendations for user {}", userId);

        // Последние взаимодействия пользователя
        List<Interaction> recentInteractions = interactionRepository.findTopNByUserId(
                userId,
                PageRequest.of(0, N_MAX_RECENT_INTERACTIONS)
        );

        if (recentInteractions.isEmpty()) {
            log.debug("User {} has no interactions", userId);
            return List.of();
        }
        log.debug("User {} has {} recent interactions", userId, recentInteractions.size());
        // ID последних мероприятий (для поиска кандидатов)
        List<Long> recentEventIds = recentInteractions.stream()
                .map(Interaction::getEventId)
                .toList();

        // ЭТАП 1, Подбор мероприятий
        List<RecommendedEventProjection> candidates = similarityRepository.findRecommendedEvents(
                recentEventIds,
                userId,
                LIMIT_SIMILARITY
        );

        if (candidates.isEmpty()) {
            return List.of();
        }

        // ЭТАП 2, Вычисление оценки
        return candidates.stream()
                .map(candidate -> predictScore(candidate, userId))
                .sorted(Comparator.comparing(RecommendedEventDto::getScore).reversed())
                .limit(maxResults)
                .toList();

    }

    private RecommendedEventDto predictScore(RecommendedEventProjection candidate,
                                             Long userId) {

        List<NeighborProjection> neighbors = similarityRepository.findNearestNeighbors(
                candidate.getEventId(),
                userId,
                K_NEIGHBORS
        );

        double predictedScore = calculatePredictedScore(neighbors);

        return RecommendedEventDto.builder()
                .eventId(candidate.getEventId())
                .score(predictedScore)
                .build();
    }

    private double calculatePredictedScore(List<NeighborProjection> neighbors) {
        if (neighbors.isEmpty()) {
            return 0.0;
        }

        double weightedSum = 0.0; // аккумулятор суммы взвешенных оценок (числитель)
        double similaritySum = 0.0; // аккумулятор суммы всех коэффициентов сходства соседей (знаменатель)

        for (NeighborProjection neighbor : neighbors) {

            Double rating =neighbor.getUserRating(); // берем оценку, которую дал пользователь соседу

            weightedSum += rating * neighbor.getSimilarity(); // прибавляем к общей сумме ВЗВЕШЕННУЮ оценку
            similaritySum += neighbor.getSimilarity(); // прибавляем коэф сходства соседа к общей сумме
        }

        // рассчитываем предсказанную оценку
        return similaritySum == 0 ? 0 : weightedSum / similaritySum;
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

        return interactionRepository.findTotalWeightsByEventIds(eventIds);

    }
}