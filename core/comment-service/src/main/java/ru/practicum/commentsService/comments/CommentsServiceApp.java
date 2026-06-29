package ru.practicum.commentsService.comments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages = {
        "ru.practicum.commentsService",
        "ru.practicum.common"
})
@EnableAspectJAutoProxy // для AOP, логирование через аннотации @Loggable
public class CommentsServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(CommentsServiceApp.class, args);
    }
}