package ru.practicum.requestsService.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableFeignClients
@EnableAspectJAutoProxy // для AOP, логирование через аннотации @Loggable
@ComponentScan(basePackages = {
        "ru.practicum.requestsService.request",
        "ru.practicum.common"
})
public class RequestsServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(RequestsServiceApp.class, args);
    }
}