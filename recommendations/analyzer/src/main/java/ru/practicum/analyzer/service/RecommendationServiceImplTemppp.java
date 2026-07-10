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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
//@Service
@RequiredArgsConstructor
public class RecommendationServiceImplTemppp implements RecommendationService {

    private final InteractionRepository interactionRepository;
    private final SimilarityRepository similarityRepository;

    private static final int N_MAX_RECENT_INTERACTIONS = 10;
    private static final int LIMIT_SIMILARITY  = 10;
    private static final int K_NEIGHBORS = 5;

    // ==================== ГЛАВНЫЙ МЕТОД ====================

    @Transactional(readOnly = true)
    @Override
    public List<RecommendedEventDto> getRecommendationsForUser(Long userId, int maxResults) {
        log.debug("Getting recommendations for user: {}", userId);

        // 1. Получаем последние взаимодействия пользователя
        List<Interaction> userInteractions = interactionRepository.findTopNByUserId(userId,
                PageRequest.of(0, N_MAX_RECENT_INTERACTIONS));
        if (userInteractions.isEmpty()) {
            log.debug("No interactions found for user: {}, no recommendations", userId);
            return List.of();
        }

        // 2. Создаем контекст пользователя
        // Мапа: просмотренное событие → рейтинг пользователя
        Map<Long, Double> userRatings = userInteractions.stream()
                .collect(Collectors.toMap(
                        Interaction::getEventId,
                        Interaction::getWeight,
                        (a, b) -> a
                ));

        // Множество ID мероприятий, с которыми пользователь уже взаимодействовал
//        Set<Long> interactedEvents = new HashSet<>(userRatings.keySet());
        Set<Long> interactedEvents = userInteractions.stream()
                .map(Interaction::getEventId)
                .collect(Collectors.toSet());

        // 3. Этап 1: Находим кандидатов (мероприятия, которые будем рекомендовать)
        List<Candidate> candidates = findCandidates(interactedEvents);

        // 4. Этап 2: Предсказываем оценки для кандидатов
        List<RecommendedEventDto> recommendations = predictScores(candidates, userRatings);

        // 5. Сортируем по предсказанной оценке и ограничиваем
        return sortAndLimit(recommendations, maxResults);
    }


    // ==================== ЭТАП 1: ПОИСК КАНДИДАТОВ ====================

    private List<Candidate> findCandidates(Set<Long> interactedEvents) {
        // 1. Получаем по LIMIT_SIMILARITY похожих мероприятий для каждого просмотренного пользователем события, одним запросом
        List<Similarity> allSimilarities = similarityRepository.findTopNByEventIds(new ArrayList<>(interactedEvents), LIMIT_SIMILARITY);  // toDo вынести лимит в пропс

        // 2. Разбираем, к какому просмотренному событию какая похожесть относится, eventId -> List<Similarity>
        Map<Long, List<Similarity>> similaritiesByCandidate = groupSimilaritiesByCandidate(
                allSimilarities,
                interactedEvents
        );

        // 3. Отбираем самых похожих кандидатов, в которых еще не участовал пользователь.
        return similaritiesByCandidate.entrySet().stream()
                .map(entry -> {
                    Long candidateId = entry.getKey();
                    List<Similarity> similarities = entry.getValue();

                    // Суммируем все коэффициенты сходства
                    double totalSimilarity = similarities.stream()
                            .mapToDouble(Similarity::getSimilarity)
                            .sum();

                    return new Candidate(candidateId, similarities, totalSimilarity);
                })
                .filter(candidate -> !interactedEvents.contains(candidate.getId())) // только те, в которых не участвовал user
                .sorted(Comparator.comparing(Candidate::getTotalSimilarity).reversed())
                .limit(N_MAX_RECENT_INTERACTIONS) // ограничиваем кандидатов
                .collect(Collectors.toList());
    }

    private Map<Long, List<Similarity>> groupSimilaritiesByCandidate( List<Similarity> allSimilarities,
                                                                      Set<Long> interactedEvents) {

        Map<Long, List<Similarity>> result = new HashMap<>();

        for (Similarity sim : allSimilarities) {
            // Определяем исходное мероприятие (из просмотренных пользователем)
            Long sourceId = interactedEvents.contains(sim.getEvent1()) ? sim.getEvent1() : sim.getEvent2();

            // Определяем кандидата (другое мероприятие)
            Long candidateId = sourceId.equals(sim.getEvent1()) ? sim.getEvent2() : sim.getEvent1();

            // Пропускаем, если кандидат уже просмотрен пользователем
            if (interactedEvents.contains(candidateId)) {
                continue;
            }

            result.computeIfAbsent(candidateId, k -> new ArrayList<>()).add(sim);
        }

        return result;
    }

    // ==================== ЭТАП 2: ПРЕДСКАЗАНИЕ ОЦЕНОК ====================

    private List<RecommendedEventDto> predictScores(List<Candidate> candidates, Map<Long, Double> userRatings) {
        List<RecommendedEventDto> result = new ArrayList<>();

        for (Candidate candidate : candidates) {
            double predictedScore = predictScoreForCandidate(candidate, userRatings);
            result.add(RecommendedEventDto.builder()
                    .eventId(candidate.getId())
                    .score(predictedScore)
                    .build());
        }

        return result;
    }

    private double predictScoreForCandidate(Candidate candidate, Map<Long, Double> userRatings) {
        Long candidateId = candidate.getId();
        List<Similarity> allSimilarities = candidate.getSimilarities();

        // 1. Находим K ближайших соседей (максимально похожие на кандидата)
        List<Similarity> nearestNeighbors = findNearestNeighbors(allSimilarities, K_NEIGHBORS);

        if (nearestNeighbors.isEmpty()) {
            log.debug("No neighbors found for candidate: {}", candidateId);
            return 0.0;
        }

        // 2. Вычисляем взвешенную оценку
        double weightedSum = calculateWeightedSum(nearestNeighbors, userRatings);
        double similaritySum = calculateSimilaritySum(nearestNeighbors);

        // 3. Нормализуем и возвращаем предсказанную оценку
        return normalizeScore(weightedSum, similaritySum);
    }

    private List<Similarity> findNearestNeighbors(List<Similarity> similarities, int k) {
        return similarities.stream()
                .sorted(Comparator.comparing(Similarity::getSimilarity).reversed())
                .limit(k)
                .collect(Collectors.toList());
    }

    private double calculateWeightedSum(List<Similarity> neighbors, Map<Long, Double> userRatings) {
        double weightedSum = 0.0;

        for (Similarity neighbor : neighbors) {
            // Определяем ID соседнего мероприятия (не кандидата)
            // Предполагаем, что кандидат участвует в паре, а сосед — другое мероприятие
            // Здесь нужно определить, какое мероприятие является соседом
            Long neighborId = getNeighborId(neighbor);

            Double rating = userRatings.get(neighborId);
            if (rating == null) {
                continue; // Пропускаем, если оценки нет
            }

            weightedSum += neighbor.getSimilarity() * rating;
        }

        return weightedSum;
    }

    private double calculateSimilaritySum(List<Similarity> neighbors) {
        return neighbors.stream()
                .mapToDouble(Similarity::getSimilarity)
                .sum();
    }

    private double normalizeScore(double weightedSum, double similaritySum) {
        if (similaritySum == 0.0) {
            return 0.0;
        }
        return weightedSum / similaritySum;
    }

    private Long getNeighborId(Similarity similarity) {
        // В зависимости от контекста определяем, какое мероприятие является соседом
        // Для кандидата нужно вернуть ID другого мероприятия
        // Этот метод будет использоваться в контексте конкретного кандидата
        throw new UnsupportedOperationException("Should be overridden with specific logic");
    }

    // ==================== ШАГ 3: СОРТИРОВКА И ОГРАНИЧЕНИЕ ====================

    private List<RecommendedEventDto> sortAndLimit(List<RecommendedEventDto> recommendations, int maxResults) {
        return recommendations.stream()
                .sorted(Comparator.comparing(RecommendedEventDto::getScore).reversed())
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ КЛАССЫ ====================


    @Data
    @AllArgsConstructor
    private static class Candidate {
        private Long id;
        private List<Similarity> similarities;
        private double totalSimilarity;
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