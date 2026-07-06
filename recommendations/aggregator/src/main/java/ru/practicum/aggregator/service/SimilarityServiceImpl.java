package ru.practicum.aggregator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.aggregator.model.ActionWeight;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class SimilarityServiceImpl implements SimilarityService {
    private final Map<Long, Map<Long, Double>> userEventWeights = new HashMap<>(); // Event ->  <User, Weight>

    private final Map<Long, Double> eventTotalWeights = new HashMap<>(); // Event ->  TotalWeight

    private final Map<Long, Map<Long, Double>> pairMinWeightSums = new HashMap<>(); // EventA ->  <EventB, MinWeightSum>


    @Override
    public void initSimilarityCalculation(UserActionAvro userActionAvro) {
        double actionWeight = ActionWeight.getWeight(userActionAvro.getActionType());
        double savedActionWeight = userEventWeights.getOrDefault(userActionAvro.getEventId(), new HashMap<>()).getOrDefault(userActionAvro.getUserId(), 0.0);
        if(actionWeight <= savedActionWeight){
            return;
        }

    }
}
