package ru.practicum.analyzer.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class KafkaConfiguration {

    private final KafkaProps kafkaProps;

    @Bean
    public KafkaConsumer<String, EventSimilarityAvro> similarityConsumer() {
        KafkaConsumer<String, EventSimilarityAvro> consumer = new KafkaConsumer<>(
                kafkaProps.getConsumer().getSimilarity().getProperties()
        );
        consumer.subscribe(List.of(kafkaProps.getConsumer().getSimilarity().getTopic()));
        return consumer;
    }

    @Bean
    public KafkaConsumer<String, UserActionAvro> userActionConsumer() {
        KafkaConsumer<String, UserActionAvro> consumer = new KafkaConsumer<>(
                kafkaProps.getConsumer().getUserAction().getProperties()
        );
        consumer.subscribe(List.of(kafkaProps.getConsumer().getUserAction().getTopic()));
        return consumer;
    }
}