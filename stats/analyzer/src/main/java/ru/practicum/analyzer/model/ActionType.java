package ru.practicum.analyzer.model;

import lombok.Getter;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;

@Getter
public enum ActionType {

    VIEW(0.4),
    REGISTER(0.8),
    LIKE(1.0);

    private final double weight;

    ActionType(double weight) {
        this.weight = weight;
    }

    public static double getWeight(ActionTypeAvro actionType) {
        if (actionType == null) {
            return 0.0;
        }
        return switch (actionType) {
            case VIEW -> VIEW.weight;
            case REGISTER -> REGISTER.weight;
            case LIKE -> LIKE.weight;
        };
    }

    public static double getWeight(String actionType) {
        if (actionType == null || actionType.isBlank()) {
            return 0.0;
        }
        try {
            ActionTypeAvro avroType = ActionTypeAvro.valueOf(actionType.toUpperCase());
            return getWeight(avroType);
        } catch (IllegalArgumentException e) {
            return 0.0;
        }
    }
}