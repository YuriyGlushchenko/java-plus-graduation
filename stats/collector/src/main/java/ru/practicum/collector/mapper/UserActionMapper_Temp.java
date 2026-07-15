package ru.practicum.collector.mapper;

import com.google.protobuf.Timestamp;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.recommendations.proto.ActionTypeProto;
import ru.practicum.recommendations.proto.UserActionProto;

import java.time.Instant;


public class UserActionMapper_Temp {

    public UserActionAvro toAvro(UserActionProto proto) {
        if (proto == null) {
            return null;
        }

        return UserActionAvro.newBuilder()
                .setUserId(proto.getUserId())
                .setEventId(proto.getEventId())
                .setActionType(mapActionType(proto.getActionType()))
                .setTimestamp(mapTimestamp(proto.getTimestamp()))
                .build();
    }

    private Instant mapTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }

        long seconds = timestamp.getSeconds();
        int nanos = timestamp.getNanos();
        int millis = nanos / 1_000_000;

        return Instant.ofEpochSecond(seconds, millis * 1_000_000);
    }

    private ActionTypeAvro mapActionType(ActionTypeProto proto) {
        if (proto == null) {
            return null;
        }

        return switch (proto) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            default -> throw new IllegalArgumentException("Unknown action type: " + proto);
        };
    }
}