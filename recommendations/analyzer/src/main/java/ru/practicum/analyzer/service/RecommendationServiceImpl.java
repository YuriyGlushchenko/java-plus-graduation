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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final InteractionRepository interactionRepository;
    private final SimilarityRepository similarityRepository;

    @Transactional(readOnly = true)
    @Override
    public List<RecommendedEventDto> getRecommendationsForUser(Long userId, int maxResults) {
        log.debug("Getting recommendations for user: {}", userId);

        // 1. Получаем последние N взаимодействий пользователя
        List<Interaction> userInteractions = interactionRepository
                .findTopNByUserId(userId, PageRequest.of(0, 10));

        if (userInteractions.isEmpty()) {
            log.debug("No interactions found for user: {}", userId);
            return List.of(); // Пользователь не взаимодействовал с мероприятиями, рекомендовать нечего
        }

        // 2. Достаем ID мероприятий, с из полученного списка
        Set<Long> interactedEvents = userInteractions.stream()
                .map(Interaction::getEventId)
                .collect(Collectors.toSet());

        // 3. Для каждого просмотренного мероприятия ищем похожие
        Map<Long, Double> candidateScores = new HashMap<>();

        for (Interaction interaction : userInteractions) {
            Long eventId = interaction.getEventId();

            // Получаем похожие мероприятия для этого eventId
            List<Similarity> similarities = similarityRepository.findByEventId(eventId);

            for (Similarity sim : similarities) {
                Long candidateId = sim.getEvent1().equals(eventId) ? sim.getEvent2() : sim.getEvent1();

                // Исключаем мероприятия, с которыми пользователь уже взаимодействовал
                if (interactedEvents.contains(candidateId)) {
                    continue;
                }

                // Суммируем оценки (чем больше похожих мероприятий, тем выше оценка)
                // возможно тут стоит рассмотреть max а не sum? но sum логичнее по смыслу
                candidateScores.merge(candidateId, sim.getSimilarity(), Double::sum);
            }
        }

        // 4. Сортируем по убыванию оценки и ограничиваем количество
        return candidateScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(entry -> RecommendedEventDto.builder()
                        .eventId(entry.getKey())
                        .score(entry.getValue())
                        .build())
                .collect(Collectors.toList());
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