package ru.practicum.analyzer.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Properties;

@Getter
@AllArgsConstructor
@ConfigurationProperties("kafka")
public class KafkaProps {
    private final ProducerConfig producer;
    private final ConsumerConfig consumer;


    @Setter
    @Getter
    @AllArgsConstructor
    public static class ProducerConfig {
        private Properties properties;
    }

    @Setter
    @Getter
    @AllArgsConstructor
    public static class ConsumerConfig {
        private Duration pollTimeout;
        private SimilarityConsumerProps similarity;
        private UserActionConsumerProps userAction;
    }

    @Setter
    @Getter
    @AllArgsConstructor
    public static class SimilarityConsumerProps {
        private String topic;
        private Properties properties;
    }

    @Setter
    @Getter
    @AllArgsConstructor
    public static class UserActionConsumerProps {
        private String topic;
        private Properties properties;
    }
}