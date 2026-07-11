package ru.practicum.stat.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stat.client.exception.StatsServerUnavailableException;
import ru.practicum.stat.dto.EndpointHitDto;
import ru.practicum.stat.dto.ParamDto;
import ru.practicum.stat.dto.ViewStatsDto;

import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Component
@Slf4j
public class StatsClient {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate template;
    private final DiscoveryClient discoveryClient;
    private final String statsServiceId;
    private final String appName;
    private final RetryTemplate retryTemplate;

    public StatsClient(
            RestTemplate template,
            DiscoveryClient discoveryClient,
            @Value("${stats-service.id:stats-server}") String statsServiceId,
            @Value("${app.name:main-service}") String appName
    ) {
        this.template = template;
        this.discoveryClient = discoveryClient;
        this.statsServiceId = statsServiceId;
        this.appName = appName;
        this.retryTemplate = createRetryTemplate();
    }

    private RetryTemplate createRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // задержка между попытками (3 секунды)
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(3000L);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        // Максимальное количество попыток (3)
        MaxAttemptsRetryPolicy retryPolicy = new MaxAttemptsRetryPolicy(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        log.info("RetryTemplate инициализирован: maxAttempts=3, backOffPeriod=3000ms");
        return retryTemplate;
    }

    private ServiceInstance getInstance() {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(statsServiceId);
            if (instances.isEmpty()) {
                throw new NoSuchElementException("Нет доступных экземпляров сервиса: " + statsServiceId);
            }
            ServiceInstance instance = instances.getFirst();
            log.debug("Найден экземпляр сервиса статистики: {}:{}",
                    instance.getHost(), instance.getPort());
            return instance;
        } catch (Exception exception) {
            throw new StatsServerUnavailableException(
                    "Ошибка обнаружения адреса сервиса статистики с id: " + statsServiceId,
                    exception
            );
        }
    }

    private URI makeUri(String path) {
        try {
            ServiceInstance instance = retryTemplate.execute(context -> {
                if (context.getRetryCount() > 0) {
                    log.info("Попытка #{} получить экземпляр сервиса '{}'", context.getRetryCount() + 1, statsServiceId);
                }
                return getInstance();
            });
            return URI.create("http://" + instance.getHost() + ":" + instance.getPort() + path);
        } catch (StatsServerUnavailableException e) {
            log.error("Не удалось получить экземпляр сервиса статистики");
            throw e;
        }
    }

    public void hit(EndpointHitDto endpointHit) {
        endpointHit.setApp(appName);
        try {
            URI uri = makeUri("/hit");
            HttpEntity<EndpointHitDto> requestEntity = new HttpEntity<>(endpointHit);
            template.exchange(uri, POST, requestEntity, Object.class);
            log.debug("Хит успешно отправлен на сервер статистики");
        } catch (RestClientException e) {
            log.warn("Не удалось сохранить хит: {}", e.getMessage());
        } catch (StatsServerUnavailableException e) {
            log.warn("Сервер статистики недоступен: {}", e.getMessage());
        }
    }

    public List<ViewStatsDto> get(ParamDto paramDto) {
        try {
            URI uri = makeUri("/stats");

            URI fullUri = UriComponentsBuilder
                    .fromUri(uri)
                    .queryParam("start", paramDto.getStart().format(FORMATTER))
                    .queryParam("end", paramDto.getEnd().format(FORMATTER))
                    .queryParam("uris", (Object[]) paramDto.getUris())
                    .queryParam("unique", paramDto.getUnique())
                    .build()
                    .encode()
                    .toUri();

            ResponseEntity<List<ViewStatsDto>> response = template.exchange(
                    fullUri,
                    GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );

            log.info("=== STATS CLIENT RESPONSE: status={}, body={} ===",
                    response.getStatusCode(), response.getBody());

            return response.getBody();
        } catch (RestClientException e) {
            log.warn("Ошибка при получении статистики: {}.", e.getMessage());
            return List.of(ViewStatsDto.builder().hits(-1L).build());
        } catch (StatsServerUnavailableException e) {
            log.warn("Сервер статистики недоступен: {}", e.getMessage());
            return List.of(ViewStatsDto.builder().hits(-1L).build());
        }
    }
}