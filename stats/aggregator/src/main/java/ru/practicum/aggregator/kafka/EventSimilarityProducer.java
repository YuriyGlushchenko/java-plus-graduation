package ru.practicum.aggregator.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Component;

import ru.practicum.aggregator.config.KafkaProps;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityProducer {

    private final Producer<String, SpecificRecordBase> producer;
    private final KafkaProps kafkaProps;

    public CompletableFuture<RecordMetadata> sendEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        String key = String.valueOf(eventSimilarityAvro.getEventA());
        long timestamp = eventSimilarityAvro.getTimestamp().toEpochMilli();

        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                kafkaProps.getProducer().getTopic(),
                null, // partition — передаём null, чтобы Kafka сам определил партицию по ключу
                timestamp, // используем timestamp самого действия (влияет на порядок сообщений в брокере, если ключ одинаковый)
                key,
                eventSimilarityAvro
        );

        return sendToBroker(record);
    }

    private CompletableFuture<RecordMetadata> sendToBroker(ProducerRecord<String, SpecificRecordBase> record) {

        // CompletableFuture - аналог Promise из JS для работы с асинхронным send. Удобно использовать, не блокирует поток.
        CompletableFuture<RecordMetadata> future = new CompletableFuture<>();

        producer.send(record, (metadata, exception) -> {
            if (exception == null) {
                // когда send вернет metadata, резолвим созданный CompletableFuture и кладем в него эту metadata
                future.complete(metadata);

            } else {
                future.completeExceptionally(exception);
            }
        });

        return future;
    }

}