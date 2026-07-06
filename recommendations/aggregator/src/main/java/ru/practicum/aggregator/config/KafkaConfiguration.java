package ru.practicum.aggregator.config;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.KafkaConsumer;
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

    @Bean
    public KafkaConsumer<String, SpecificRecordBase> kafkaConsumer() {
        KafkaConsumer<String, SpecificRecordBase> consumer = new KafkaConsumer<>(
                kafkaProps.getConsumer().getProperties()
        );
        consumer.subscribe(java.util.List.of(kafkaProps.getConsumer().getTopic()));
        return consumer;
    }
}