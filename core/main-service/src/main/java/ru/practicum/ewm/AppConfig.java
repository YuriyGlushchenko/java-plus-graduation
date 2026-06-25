package ru.practicum.ewm;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class AppConfig {

    private final EntityManager entityManager;

    public AppConfig(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // бин JPAQueryFactory для QueryDSL
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }

    // бин RestTemplate для StatsClient
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }

    // Если хотите, можно сразу создать и бин StatsClient
    /*
    @Bean
    public StatsClient statsClient(RestTemplate restTemplate,
                                   @Value("${stats-server.url}") String url) {
        return new StatsClient(restTemplate, url);
    }
    */
}