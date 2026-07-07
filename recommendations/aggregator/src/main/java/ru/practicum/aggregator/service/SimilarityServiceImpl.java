package ru.practicum.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.aggregator.model.ActionWeight;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.min;
import static java.lang.Math.sqrt;

@RequiredArgsConstructor
@Service
@Slf4j
public class SimilarityServiceImpl implements SimilarityService {
    private final Map<Long, Map<Long, Double>> userEventWeights = new HashMap<>(); // МАТРИЦА Event ->  <User, Weight>

    private final Map<Long, Double> eventTotalWeights = new HashMap<>(); // ВЕС СЕБЫТИЯ, Event ->  TotalWeight

    // Пары минимальных весов между двух событий
    private final Map<Long, Map<Long, Double>> pairMinWeightSums = new HashMap<>(); // EventA ->  <EventB, MinWeightSum>


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
        eventTotalWeights.merge(eventId, delta, Double::sum);

        // 3. Получаем список событий (кроме текущего) с которыми взаимодействовал пользователь,
        // а значит вносил свой вес и вляил на сходство, которое теперь надо пересчитать:
        List<Long> eventsToUpdate = userEventWeights.entrySet().stream()
                .filter(entry -> {
                    Long event = entry.getKey();
                    Map<Long, Double> userWeights = entry.getValue();
                    return event != eventId
                            && userWeights != null
                            && userWeights.containsKey(userId)
                            && userWeights.get(userId) > 0 ;
                })
                .map(Map.Entry::getKey)
                .forEach(id -> proccessEventPair(eventId, userId, actionWeight, oldWeight, id ) );
















    }


    private void proccessEventPair(long actionEvent, long userId, double actionWeight, double oldActionWeight, long pairEvent){
        double pairEventWeight = userEventWeights.get(pairEvent).get(userId);
        double oldMinPairWeight = min(oldActionWeight, pairEventWeight);
        double newMinPairWeight = min(actionWeight, pairEventWeight);

        double delta = newMinPairWeight - oldMinPairWeight;

        double sum = getMinSum(actionEvent, pairEvent);
        if(delta >= 0){
            sum += delta;
            putMinSum(actionEvent, pairEvent, sum);
        }

        double similarity = sum / (sqrt(eventTotalWeights.get(actionEvent)) * sqrt(eventTotalWeights.get(pairEvent)));





    }

    /**
     * Получить сохраненный вес пользователя для мероприятия
     */
    private double getUserWeight(long eventId, long userId) {
        Map<Long, Double> userWeights = userEventWeights.get(eventId);
        if (userWeights == null) {
            return 0.0;
        }
        return userWeights.getOrDefault(userId, 0.0);
    }

    /**
     * Пересчитать сходство для всех пар с мероприятием eventA
     */
    private void recalculateSimilarities(long eventA, Instant timestamp) {
        double totalA = eventTotalWeights.getOrDefault(eventA, 0.0);

        // Если общая сумма весов равна 0 - сходство не считается
        if (totalA <= 0) {
            log.debug("Total weight for event {} is zero, skip similarity calculation", eventA);
            return;
        }

        // Получаем всех пользователей мероприятия A
        Map<Long, Double> usersA = userEventWeights.getOrDefault(eventA, Map.of());

        // Для каждого мероприятия B (кроме A) пересчитываем сходство
        for (Long eventB : eventTotalWeights.keySet()) {
//            if (eventA.equals(eventB)) {
//                continue;
//            }

            double totalB = eventTotalWeights.getOrDefault(eventB, 0.0);
            if (totalB <= 0) {
                continue;
            }

            // Получаем пользователей мероприятия B
            Map<Long, Double> usersB = userEventWeights.getOrDefault(eventB, Map.of());

            // Вычисляем S_min - сумму минимальных весов для общих пользователей
            double minSum = calculateMinSum(usersA, usersB);

            // Сохраняем S_min для пары (упорядочиваем идентификаторы)
            putMinSum(eventA, eventB, minSum);

            // Вычисляем косинусное сходство
            double similarity = minSum / (sqrt(totalA) * sqrt(totalB));

            // Отправляем результат
            sendSimilarity(eventA, eventB, similarity, timestamp);

            log.debug("Similarity calculated: eventA={}, eventB={}, minSum={}, totalA={}, totalB={}, similarity={}",
                    eventA, eventB, minSum, totalA, totalB, similarity);
        }
    }

    /**
     * Вычислить сумму минимальных весов для общих пользователей
     */
    private double calculateMinSum(Map<Long, Double> usersA, Map<Long, Double> usersB) {
        double sum = 0.0;

        // Идем по меньшей мапе для оптимизации
        if (usersA.size() > usersB.size()) {
            Map<Long, Double> temp = usersA;
            usersA = usersB;
            usersB = temp;
        }

        for (Map.Entry<Long, Double> entryA : usersA.entrySet()) {
            Long userId = entryA.getKey();
            Double weightA = entryA.getValue();
            Double weightB = usersB.get(userId);

            if (weightB != null) {
                sum += min(weightA, weightB);
            }
        }

        return sum;
    }

    /**
     * Сохранить сумму минимальных весов для пары (упорядоченные идентификаторы)
     */
    private void putMinSum(long eventA, long eventB, double sum) {
        long first = min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        pairMinWeightSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .put(second, sum);
    }

    /**
     * Получить сумму минимальных весов для пары (упорядоченные идентификаторы)
     */
    private double getMinSum(long eventA, long eventB) {
        long first = min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        Map<Long, Double> innerMap = pairMinWeightSums.get(first);
        if (innerMap == null) {
            return 0.0;
        }
        return innerMap.getOrDefault(second, 0.0);
    }

    /**
     * Отправить сходство в Kafka
     */
    private void sendSimilarity(long eventA, long eventB, double similarity, Instant timestamp) {
        // Упорядочиваем идентификаторы
        long first = min(eventA, eventB);
        long second = Math.max(eventA, eventB);

//        producer.sendEventSimilarity(first, second, similarity, timestamp);
    }

}
