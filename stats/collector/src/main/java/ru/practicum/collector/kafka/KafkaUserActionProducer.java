package ru.practicum.collector.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Component;
import ru.practicum.collector.config.KafkaProps;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaUserActionProducer {

    private final Producer<Long, SpecificRecordBase> producer;
    private final KafkaProps kafkaProps;

    public CompletableFuture<RecordMetadata> sendUserAction(UserActionAvro userActionAvro) {
        Long key = userActionAvro.getUserId();
        long timestamp = userActionAvro.getTimestamp().toEpochMilli();

        ProducerRecord<Long, SpecificRecordBase> record = new ProducerRecord<>(
                kafkaProps.getProducer().getUserActionTopic(),
                null, // partition — передаём null, чтобы Kafka сам определил партицию по ключу
                timestamp, // используем timestamp самого действия (влияет на порядок сообщений в брокере, если ключ одинаковый)
                key,
                userActionAvro
        );

        return sendToBroker(record);
    }

    private CompletableFuture<RecordMetadata> sendToBroker(ProducerRecord<Long, SpecificRecordBase> record) {

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