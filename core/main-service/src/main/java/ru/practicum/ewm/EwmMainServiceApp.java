package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@ComponentScan(basePackages = {
        "ru.practicum.ewm",
        "ru.practicum.events",
        "ru.practicum.requests",
        "ru.practicum.users",
        "ru.practicum.comments",
        "ru.practicum.stat"         
})
@EntityScan({
        "ru.practicum.events.model",
        "ru.practicum.requests.model",
        "ru.practicum.users.model",
        "ru.practicum.comments.model"
})
@EnableJpaRepositories({
        "ru.practicum.events.repository",
        "ru.practicum.requests.repository",
        "ru.practicum.users.repository",
        "ru.practicum.comments.repository"
})
@EnableFeignClients
public class EwmMainServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(EwmMainServiceApp.class, args);
    }
}