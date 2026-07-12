package ru.practicum.analyzer.grpc.mapper;

import org.mapstruct.Mapper;
import ru.practicum.analyzer.model.RecommendedEventDto;
import ru.practicum.recommendations.proto.RecommendedEventProto;

@Mapper(componentModel = "spring")
public interface RecommendationEventMapper {

    RecommendedEventProto toProto(RecommendedEventDto dto);

}