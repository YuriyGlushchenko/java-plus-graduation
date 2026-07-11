package ru.practicum.eventsService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableFeignClients
@EnableAspectJAutoProxy // для AOP, логирование через аннотации @Loggable
@ComponentScan(basePackages = {
        "ru.practicum.eventsService",
        "ru.practicum.stats",
        "ru.practicum.common",
        "ru.practicum.clients"
})
public class EventsServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(EventsServiceApp.class, args);
    }
}