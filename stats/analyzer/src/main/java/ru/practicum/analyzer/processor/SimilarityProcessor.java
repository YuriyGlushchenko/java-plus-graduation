package ru.practicum.analyzer.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.config.KafkaProps;
import ru.practicum.analyzer.service.SimilarityService;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import jakarta.annotation.PostConstruct;

@Slf4j
@Component
public class SimilarityProcessor extends BaseProcessor<EventSimilarityAvro>  {

    private final SimilarityService similarityService;

    public SimilarityProcessor(
            KafkaProps kafkaProps,
            KafkaConsumer<String, EventSimilarityAvro> consumer,
            SimilarityService similarityService) {
        super(kafkaProps, consumer);
        this.similarityService = similarityService;
    }

    @Override
    protected void handleRecord(ConsumerRecord<String, EventSimilarityAvro> record) {
        EventSimilarityAvro value = record.value();

        if (value == null) {
            log.warn("Получено пустое сообщение");
            return;
        }

        log.debug("Получено EventSimilarity: eventA={}, eventB={}, score={}",
                value.getEventA(), value.getEventB(), value.getScore());


        similarityService.saveSimilarity(value);
    }
}