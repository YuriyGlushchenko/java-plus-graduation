package ru.practicum.collector.mapper;

import com.google.protobuf.Timestamp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.recommendations.proto.ActionTypeProto;
import ru.practicum.recommendations.proto.UserActionProto;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface UserActionMapper {

    @Mapping(source = "timestamp", target = "timestamp")
    UserActionAvro toAvro(UserActionProto proto);

    default Instant map(Timestamp timestamp) {
        if (timestamp == null) return null;
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }

    // MapStruct сам автоматом найдет этот метод по типам входящих параметров и возвращаемого значения
    default ActionTypeAvro mapActionType(ActionTypeProto proto) {
        if (proto == null) return null;
        return switch (proto) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            default -> throw new IllegalArgumentException("Unknown action type: " + proto);
        };
    }
}