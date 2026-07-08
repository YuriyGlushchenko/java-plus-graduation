package ru.practicum.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.aggregator.kafka.EventSimilarityProducer;
import ru.practicum.aggregator.model.ActionWeight;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.max;
import static java.lang.Math.min;

@RequiredArgsConstructor
@Service
@Slf4j
public class SimilarityServiceImpl implements SimilarityService {

    private final EventSimilarityProducer producer;

    private final Map<Long, Map<Long, Double>> userEventWeights = new HashMap<>(); // Event -> <User, Weight>
    private final Map<Long, Double> eventTotalWeights = new HashMap<>(); // Event -> TotalWeight
    private final Map<Long, Map<Long, Double>> pairMinWeightSums = new HashMap<>(); // EventA -> <EventB, MinWeightSum>

    @Override
    public void processUserAction(UserActionAvro userActionAvro) {
        long eventId = userActionAvro.getEventId();
        long userId = userActionAvro.getUserId();
        double actionWeight = ActionWeight.getWeight(userActionAvro.getActionType());
        double oldWeight = getUserWeight(eventId, userId);

        if (actionWeight <= oldWeight) {
            log.debug("Weight not changed, no action for event={}, user={}, weight={}", eventId, userId, oldWeight);
            return;
        }

        log.debug("Updating weight: event={}, user={}, old={}, new={}", eventId, userId, oldWeight, actionWeight);

        // 1. Обновляем данные пользователя
        double newTotalA = updateUserWeight(eventId, userId, actionWeight, oldWeight);

        // 2. Получаем список событий (кроме текущего) с которыми взаимодействовал пользователь,
        // а значит вносил свой вес и вляил на сходство, которое теперь надо пересчитать:
        List<Long> eventsToUpdate = getEventsToUpdate(eventId, userId);

        // 3. Пересчитываем сходство для каждой пары
        recalculateSimilarities(eventId, userId, actionWeight, oldWeight, newTotalA,
                eventsToUpdate, userActionAvro.getTimestamp());
    }

    private double updateUserWeight(long eventId, long userId, double newWeight, double oldWeight) {
        double delta = newWeight - oldWeight;

        // Обновляем матрицу
        userEventWeights.computeIfAbsent(eventId, e -> new HashMap<>())
                .put(userId, newWeight);

        // Обновляем общую сумму весов события
        double oldTotal = eventTotalWeights.getOrDefault(eventId, 0.0);
        double newTotal = oldTotal + delta;
        eventTotalWeights.put(eventId, newTotal);

        return newTotal;
    }

    private List<Long> getEventsToUpdate(long currentEventId, long userId) {
        return userEventWeights.entrySet().stream()
                .filter(entry -> {
                    Long eventId = entry.getKey();
                    Map<Long, Double> userWeights = entry.getValue();
                    return eventId != currentEventId
                            && userWeights != null
                            && userWeights.containsKey(userId)
                            && userWeights.get(userId) > 0;
                })
                .map(Map.Entry::getKey)
                .toList();
    }

    private void recalculateSimilarities(long eventA, long userId, double newWeightA,
                                         double oldWeightA, double newTotalA,
                                         List<Long> eventsToUpdate, Instant timestamp) {
        for (Long eventB : eventsToUpdate) {
            double weightB = getUserWeight(eventB, userId);
            double result = calculatePairSimilarity(eventA, eventB, newWeightA, oldWeightA, weightB, newTotalA);

            sendSimilarity(eventA, eventB, result, timestamp);
            log.debug("Updated similarity: A={}, B={}, similarity={}", eventA, eventB, result);
        }
    }

    private double calculatePairSimilarity(long eventA, long eventB,
                                           double newWeightA, double oldWeightA,
                                           double weightB, double totalA) {
        // 1. Вычисляем изменение S_min
        double oldMin = min(oldWeightA, weightB);
        double newMin = min(newWeightA, weightB);
        double deltaMin = newMin - oldMin;

        // 2. Обновляем S_min если нужно
        double oldSMin = getMinSum(eventA, eventB);
        double newSMin = oldSMin;
        if (deltaMin > 0) {
            newSMin = oldSMin + deltaMin;
            putMinSum(eventA, eventB, newSMin);
        }

        // 3. Вычисляем сходство
        double totalB = eventTotalWeights.getOrDefault(eventB, 0.0);
        return newSMin / (Math.sqrt(totalA) * Math.sqrt(totalB));
    }

    private double getUserWeight(long eventId, long userId) {
        Map<Long, Double> userWeights = userEventWeights.get(eventId);
        if (userWeights == null) {
            return 0.0;
        }
        return userWeights.getOrDefault(userId, 0.0);
    }

    private double getMinSum(long eventA, long eventB) {
        long first = min(eventA, eventB);
        long second = max(eventA, eventB);

        Map<Long, Double> innerMap = pairMinWeightSums.get(first);
        if (innerMap == null) {
            return 0.0;
        }
        return innerMap.getOrDefault(second, 0.0);
    }

    private void putMinSum(long eventA, long eventB, double sum) {
        long first = min(eventA, eventB);
        long second = max(eventA, eventB);
        pairMinWeightSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .put(second, sum);
    }

    private void sendSimilarity(long eventA, long eventB, double similarity, Instant timestamp) {
        long first = min(eventA, eventB);
        long second = max(eventA, eventB);

        EventSimilarityAvro eventSimilarityAvro = EventSimilarityAvro.newBuilder()
                .setEventA(first)
                .setEventB(second)
                .setScore(similarity)
                .setTimestamp(timestamp)
                .build();

        producer.sendEventSimilarity(eventSimilarityAvro)
                .thenAccept(metadata ->
                        log.info("Event similarity sent: eventA={}, eventB={}, score={}, offset={}",
                                first, second, similarity, metadata.offset()))
                .exceptionally(exception -> {
                    log.error("Failed to send similarity for pair ({}, {})", first, second, exception);
                    return null;
                });
    }
}