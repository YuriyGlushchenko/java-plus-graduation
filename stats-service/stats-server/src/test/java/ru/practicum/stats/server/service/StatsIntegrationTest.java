package ru.practicum.stats.server.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stat.dto.EndpointHitDto;
import ru.practicum.stat.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StatsIntegrationTest {

    @Autowired
    private StatsService statsService;

    @Test
    @DisplayName("Сохраняет хит и возвращает статистику")
    void shouldSaveHitAndReturnStats() {

        LocalDateTime now = LocalDateTime.now();

        EndpointHitDto hit1 = EndpointHitDto.builder()
                .app("ewm-service")
                .uri("/events/1")
                .ip("192.168.0.1")
                .timestamp(now.minusHours(1))
                .build();

        EndpointHitDto hit2 = EndpointHitDto.builder()
                .app("ewm-service")
                .uri("/events/1")
                .ip("192.168.0.2")
                .timestamp(now.minusMinutes(30))
                .build();

        statsService.saveHit(hit1);
        statsService.saveHit(hit2);

        List<ViewStatsDto> stats = statsService.getStats(
                now.minusDays(1),
                now.plusDays(1),
                List.of("/events/1"),
                false
        );

        assertThat(stats).hasSize(1);
        assertThat(stats.get(0).getHits()).isEqualTo(2);
    }

    @Test
    @DisplayName("Учитывает unique = true (distinct ip)")
    void shouldCountUniqueIps() {

        LocalDateTime now = LocalDateTime.now();

        EndpointHitDto hit1 = EndpointHitDto.builder()
                .app("ewm-service")
                .uri("/events/2")
                .ip("10.0.0.1")
                .timestamp(now.minusHours(1))
                .build();

        EndpointHitDto hit2 = EndpointHitDto.builder()
                .app("ewm-service")
                .uri("/events/2")
                .ip("10.0.0.1") // тот же IP
                .timestamp(now.minusMinutes(30))
                .build();

        statsService.saveHit(hit1);
        statsService.saveHit(hit2);

        List<ViewStatsDto> stats = statsService.getStats(
                now.minusDays(1),
                now.plusDays(1),
                List.of("/events/2"),
                true
        );

        assertThat(stats).hasSize(1);
        assertThat(stats.get(0).getHits()).isEqualTo(1); // distinct ip
    }

    @Test
    @DisplayName("Фильтрует по uri")
    void shouldFilterByUris() {

        LocalDateTime now = LocalDateTime.now();

        statsService.saveHit(EndpointHitDto.builder()
                .app("ewm-service")
                .uri("/events/1")
                .ip("1.1.1.1")
                .timestamp(now)
                .build());

        statsService.saveHit(EndpointHitDto.builder()
                .app("ewm-service")
                .uri("/events/2")
                .ip("2.2.2.2")
                .timestamp(now)
                .build());

        List<ViewStatsDto> stats = statsService.getStats(
                now.minusDays(1),
                now.plusDays(1),
                List.of("/events/1"),
                false
        );

        assertThat(stats).hasSize(1);
        assertThat(stats.get(0).getUri()).isEqualTo("/events/1");
    }
}