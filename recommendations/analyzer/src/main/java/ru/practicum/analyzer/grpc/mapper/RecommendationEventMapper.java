package ru.practicum.analyzer.grpc.mapper;

import com.google.protobuf.Timestamp;
import org.mapstruct.Mapper;
import ru.practicum.analyzer.model.RecommendedEventDto;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.recommendations.proto.ActionTypeProto;
import ru.practicum.recommendations.proto.RecommendedEventProto;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface RecommendationEventMapper {

    RecommendedEventProto toProto(RecommendedEventDto dto);

}