package ru.practicum.collector.config;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class KafkaConfiguration {

    private final KafkaProps kafkaProps;

    @Bean
    public Producer<String, SpecificRecordBase> kafkaProducer() {
        return new KafkaProducer<>(kafkaProps.getProducer().getProperties());
    }
}