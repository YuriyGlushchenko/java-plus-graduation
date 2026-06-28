package ru.practicum.requestsService.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class RequestsServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(RequestsServiceApp.class, args);
    }
}