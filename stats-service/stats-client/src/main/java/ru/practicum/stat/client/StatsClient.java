package ru.practicum.stat.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stat.dto.ParamDto;
import ru.practicum.stat.dto.EndpointHitDto;
import ru.practicum.stat.dto.ViewStatsDto;

import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Component
@Slf4j
public class StatsClient {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    final RestTemplate template;
    final String statUrl;
    private final String appName;

    public StatsClient(RestTemplate template, @Value("${stats-server.url}") String statUrl, @Value("${app.name}") String appName) {
        this.template = template;
        this.statUrl = statUrl;
        this.appName = appName;
    }

    public void hit(EndpointHitDto endpointHit) {
        endpointHit.setApp(appName);
        try {
            HttpEntity<EndpointHitDto> requestEntity = new HttpEntity<>(endpointHit);
            template.exchange(statUrl + "/hit", POST, requestEntity, Object.class);
        } catch (RestClientException e) {
            log.warn("Не удалось сохранить хит: {}", e.getMessage());
        }
    }


    public List<ViewStatsDto> get(ParamDto paramDto) {
        URI uri = UriComponentsBuilder
                .fromUriString(statUrl)
                .path("/stats")
                .queryParam("start", paramDto.getStart().format(FORMATTER))
                .queryParam("end", paramDto.getEnd().format(FORMATTER))
                .queryParam("uris", paramDto.getUris()) // убрал (Object). Из-за этого не работала статистика
                .queryParam("unique", paramDto.getUnique())
                .build()
                .encode()
                .toUri();
                try {
            ResponseEntity<List<ViewStatsDto>> response = template.exchange(
                    uri,
                    GET,
                    null,
                    new ParameterizedTypeReference<List<ViewStatsDto>>() {}
            );

            log.info("=== STATS CLIENT RESPONSE: status={}, body={} ===",
                    response.getStatusCode(), response.getBody());

            return response.getBody();
        } catch (RestClientException e) {
            log.warn("Ошибка при получении статистики: {}.", e.getMessage());
            return List.of(ViewStatsDto.builder().hits(-1L).build());
        }
    }
}
