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

        // Если новый вес не больше сохраненного - ничего не делаем
        if (actionWeight <= oldWeight) {
            log.debug("Weight not changed for event={}, user={}, weight={}", eventId, userId, oldWeight);
            return;
        }

        double delta = actionWeight - oldWeight;
        log.debug("Updating weight: event={}, user={}, old={}, new={}, delta={}",
                eventId, userId, oldWeight, actionWeight, delta);

        // 1. Обновляем вес пользователя
        userEventWeights.computeIfAbsent(eventId, e -> new HashMap<>())
                .put(userId, actionWeight);

        // 2. Обновляем общую сумму весов мероприятия
        double oldTotalA = eventTotalWeights.getOrDefault(eventId, 0.0);
        double newTotalA = oldTotalA + delta;
        eventTotalWeights.put(eventId, newTotalA);

        // 3. Получаем список событий (кроме текущего) с которыми взаимодействовал пользователь,
        // а значит вносил свой вес и вляил на сходство, которое теперь надо пересчитать:
        List<Long> eventsToUpdate = userEventWeights.entrySet().stream()
                .filter(entry -> {
                    Long event = entry.getKey();
                    Map<Long, Double> userWeights = entry.getValue();
                    return event != eventId
                            && userWeights != null
                            && userWeights.containsKey(userId)
                            && userWeights.get(userId) > 0;
                })
                .map(Map.Entry::getKey)
                .toList();

        log.debug("Events to update for user {}: {}", userId, eventsToUpdate);

        // 4. Обрабатываем каждую пару
        for (Long pairEventId : eventsToUpdate) {
            processEventPair(eventId, userId, actionWeight, oldWeight, pairEventId, newTotalA, userActionAvro.getTimestamp());
        }
    }

    private void processEventPair(long actionEvent, long userId,
                                  double actionWeight, double oldActionWeight,
                                  long pairEvent, double newTotalA, Instant timestamp) {

        // Получаем вес пользователя для парного мероприятия.
        // Проверки на null не делаем, т.к. сюда попадают только pairEvent из уже отфильтрованного списка событий, в которых есть вес пользователя с userId
        double pairEventWeight = userEventWeights.get(pairEvent).get(userId);

        // Вычисляем изменение в S_min
        double oldMinPairWeight = Math.min(oldActionWeight, pairEventWeight);
        double newMinPairWeight = Math.min(actionWeight, pairEventWeight);
        double deltaMin = newMinPairWeight - oldMinPairWeight;

        double sMin = getMinSum(actionEvent, pairEvent);
        if (deltaMin > 0) {  // Обновляем S_min только если есть изменение
            sMin += deltaMin;
            putMinSum(actionEvent, pairEvent, sMin);
            log.debug("Updated S_min: pair({},{}), new sMin={}, delta={}", actionEvent, pairEvent, sMin, deltaMin);
        }

        // Получаем общую сумму весов для парного мероприятия
        double totalB = eventTotalWeights.getOrDefault(pairEvent, 0.0);

        // Вычисляем косинусное сходство
        double similarity = 0.0;
        if (newTotalA > 0 && totalB > 0) {
            similarity = sMin / (Math.sqrt(newTotalA) * Math.sqrt(totalB));
            log.debug("Similarity calculated: A={}, B={}, sMin={}, totalA={}, totalB={}, similarity={}",
                    actionEvent, pairEvent, sMin, newTotalA, totalB, similarity);
        } else {
            log.debug("Cannot calculate similarity: totalA={}, totalB={}", newTotalA, totalB);
        }

        sendSimilarity(actionEvent, pairEvent, similarity, timestamp);
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
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        EventSimilarityAvro eventSimilarityAvro = EventSimilarityAvro.newBuilder()
                .setEventA(first)
                .setEventB(second)
                .setScore(similarity)
                .setTimestamp(timestamp)
                .build();

        producer.sendEventSimilarity(eventSimilarityAvro)
                .thenAccept(metadata -> {
                    log.info("Event similarity sent: eventA={}, eventB={}, score={}, offset={}",
                            first, second, similarity, metadata.offset());
                })
                .exceptionally(exception -> {
                    log.error("Failed to send Event similarity for pair ({}, {})", first, second, exception);
                    return null;
                });
    }
}
