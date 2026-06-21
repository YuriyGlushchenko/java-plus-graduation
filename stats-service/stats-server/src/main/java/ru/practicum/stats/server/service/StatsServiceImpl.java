package ru.practicum.stats.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stat.dto.EndpointHitDto;
import ru.practicum.stat.dto.ViewStatsDto;
import ru.practicum.stats.server.dto.StatsMapper;
import ru.practicum.stats.server.model.EndpointHit;
import ru.practicum.stats.server.repository.StatsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final StatsRepository repository;

    @Transactional
    public void saveHit(EndpointHitDto hitDto) {
        EndpointHit hit = StatsMapper.toEntity(hitDto);
        repository.save(hit);
        log.debug("Saved hit: {} {} from {}", hit.getApp(), hit.getUri(), hit.getIp());
    }

    @Override
    public List<ViewStatsDto> getStats(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            boolean unique
    ) {
        return repository.getStats(start, end, uris, unique);
    }
}
