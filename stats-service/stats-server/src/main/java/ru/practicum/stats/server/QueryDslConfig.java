package ru.practicum.stats.server;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueryDslConfig {

    private final EntityManager entityManager;

    public QueryDslConfig(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // бин JPAQueryFactory, его мы используем в StatsRepositoryCustomImpl
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}