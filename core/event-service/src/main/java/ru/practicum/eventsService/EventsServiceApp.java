package ru.practicum.eventsService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

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