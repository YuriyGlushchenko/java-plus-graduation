package ru.practicum.stats.server.dto;

import ru.practicum.stat.dto.EndpointHitDto;
import ru.practicum.stat.dto.ViewStatsDto;
import ru.practicum.stats.server.model.EndpointHit;

public class StatsMapper {

    public static EndpointHit toEntity(EndpointHitDto hitDto) {
        return EndpointHit.builder()
                .app(hitDto.getApp())
                .uri(hitDto.getUri())
                .ip(hitDto.getIp())
                .timestamp(hitDto.getTimestamp())
                .build();
    }

    public static EndpointHitDto toHitDto(EndpointHit hit) {
        return EndpointHitDto.builder()
                .app(hit.getApp())
                .uri(hit.getUri())
                .ip(hit.getIp())
                .timestamp(hit.getTimestamp())
                .build();
    }

    public static ViewStatsDto toViewStatsDto(Object[] result) {
        // result[0] - app, result[1] - uri, result[2] - hits
        return ViewStatsDto.builder()
                .app((String) result[0])
                .uri((String) result[1])
                .hits((Long) result[2])
                .build();
    }
}