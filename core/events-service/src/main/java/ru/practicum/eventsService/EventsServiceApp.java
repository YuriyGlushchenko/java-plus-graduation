package ru.practicum.eventsService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
        "ru.practicum.ewm",
        "ru.practicum.eventsService",
        "ru.practicum.requestsService",
        "ru.practicum.userService",
        "ru.practicum.commentsService",
        "ru.practicum.stat"
})
@EntityScan(basePackages = {
        "ru.practicum.eventsService",
        "ru.practicum.requestsService",
        "ru.practicum.userService",
        "ru.practicum.commentsService"
})
@EnableJpaRepositories(basePackages = {
        "ru.practicum.eventsService",
        "ru.practicum.requestsService",
        "ru.practicum.userService",
        "ru.practicum.commentsService"
})
@EnableFeignClients
public class EventsServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(EventsServiceApp.class, args);
    }
}