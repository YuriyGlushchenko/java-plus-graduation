package ru.practicum.stats.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.stat.dto.EndpointHitDto;
import ru.practicum.stat.dto.ViewStatsDto;
import ru.practicum.stats.server.model.EndpointHit;
import ru.practicum.stats.server.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceImplTest {

    @Mock
    private StatsRepository repository;

    @InjectMocks
    private StatsServiceImpl statsService;

    private EndpointHitDto hitDto;
    private EndpointHit endpointHit;
    private LocalDateTime start;
    private LocalDateTime end;
    private List<ViewStatsDto> viewStatsList;

    @BeforeEach
    void setUp() {
        start = LocalDateTime.of(2026, 1, 1, 0, 0);
        end = LocalDateTime.of(2026, 12, 31, 23, 59);

        hitDto = EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(LocalDateTime.now())
                .build();

        endpointHit = EndpointHit.builder()
                .id(1L)
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(LocalDateTime.now())
                .build();

        viewStatsList = List.of(
                ViewStatsDto.builder()
                        .app("ewm-main-service")
                        .uri("/events/1")
                        .hits(10L)
                        .build(),
                ViewStatsDto.builder()
                        .app("ewm-main-service")
                        .uri("/events/2")
                        .hits(5L)
                        .build()
        );
    }

    @Test
    void saveHit_ShouldSaveHit_WhenValidDtoProvided() {
        when(repository.save(any(EndpointHit.class))).thenReturn(endpointHit);

        statsService.saveHit(hitDto);

        verify(repository, times(1)).save(any(EndpointHit.class));
    }

    @Test
    void saveHit_ShouldThrowException_WhenRepositoryFails() {
        when(repository.save(any(EndpointHit.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> statsService.saveHit(hitDto));

        // попытка сохранить данные была, т.е. исключение возникло после вызова save
        verify(repository, times(1)).save(any(EndpointHit.class));
    }

    @Test
    void getStats_ShouldReturnStats_WhenValidParametersProvided() {
        List<String> uris = List.of("/events/1", "/events/2");
        when(repository.getStats(start, end, uris, false)).thenReturn(viewStatsList);

        List<ViewStatsDto> result = statsService.getStats(start, end, uris, false);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("ewm-main-service", result.get(0).getApp());
        assertEquals("/events/1", result.get(0).getUri());
        assertEquals(10L, result.get(0).getHits());

        verify(repository, times(1)).getStats(start, end, uris, false);
    }

    @Test
    void getStats_ShouldReturnEmptyList_WhenNoStatsFound() {
        List<String> uris = List.of("/nonexistent");
        when(repository.getStats(start, end, uris, false)).thenReturn(List.of());

        List<ViewStatsDto> result = statsService.getStats(start, end, uris, false);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository, times(1)).getStats(start, end, uris, false);
    }

    @Test
    void getStats_ShouldReturnStatsWithUniqueIps_WhenUniqueParameterTrue() {
        List<String> uris = List.of("/events/1");
        List<ViewStatsDto> uniqueStats = List.of(
                ViewStatsDto.builder()
                        .app("ewm-main-service")
                        .uri("/events/1")
                        .hits(3L)
                        .build()
        );
        when(repository.getStats(start, end, uris, true)).thenReturn(uniqueStats);

        List<ViewStatsDto> result = statsService.getStats(start, end, uris, true);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).getHits());
        verify(repository, times(1)).getStats(start, end, uris, true);
    }

    @Test
    void getStats_ShouldReturnStatsForAllUris_WhenUrisListIsNull() {
        when(repository.getStats(start, end, null, false)).thenReturn(viewStatsList);

        List<ViewStatsDto> result = statsService.getStats(start, end, null, false);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository, times(1)).getStats(start, end, null, false);
    }

    @Test
    void getStats_ShouldReturnStatsForAllUris_WhenUrisListIsEmpty() {
        List<String> emptyUris = List.of();
        when(repository.getStats(start, end, emptyUris, false)).thenReturn(viewStatsList);

        List<ViewStatsDto> result = statsService.getStats(start, end, emptyUris, false);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository, times(1)).getStats(start, end, emptyUris, false);
    }

}