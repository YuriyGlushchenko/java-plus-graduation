package ru.practicum.analyzer.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.config.KafkaProps;
import ru.practicum.analyzer.service.InteractionService;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Slf4j
@Component

public class UserActionProcessor extends BaseProcessor<UserActionAvro> {

    private final InteractionService interactionService;

    public UserActionProcessor(
            KafkaProps kafkaProps,
            KafkaConsumer<String, UserActionAvro> consumer,
            InteractionService interactionService) {
        super(kafkaProps, consumer);
        this.interactionService = interactionService;
    }

    @Override
    protected void handleRecord(ConsumerRecord<String, UserActionAvro> record) {
        log.debug("топик = {}, партиция = {}, смещение = {}, значение: {}\n",
                record.topic(), record.partition(), record.offset(), record.value());

        UserActionAvro value = record.value();

        if (value == null) {
            log.warn("Получено пустое сообщение");
            return;
        }

        log.trace("Получено UserActionAvro : userID={}, eventID={}, actionType={}, timestamp={}",
                value.getUserId(), value.getEventId(), value.getActionType(), value.getActionType());

        interactionService.updateOrSaveInteraction(value);
    }
}
