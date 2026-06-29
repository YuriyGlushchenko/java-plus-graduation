package ru.practicum.userService.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

}