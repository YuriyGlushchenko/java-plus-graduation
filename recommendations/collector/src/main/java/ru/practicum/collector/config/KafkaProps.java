package ru.practicum.collector.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

@Getter
@AllArgsConstructor
@ConfigurationProperties("kafka")
public class KafkaProps {
    // автоматом парсит aplication.yml в java-класс, сообразно структуре класса

    private final ProducerConfig producer;

    @Setter
    @Getter
    @AllArgsConstructor
    public static class ProducerConfig {
        private String userActionTopic;
        private Properties properties;
    }
}