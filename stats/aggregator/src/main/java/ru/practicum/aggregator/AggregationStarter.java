package ru.practicum.aggregator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.aggregator.config.KafkaProps;
import ru.practicum.aggregator.service.SimilarityService;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new ConcurrentHashMap<>();
    private final KafkaProps kafkaProps;
    private final Producer<String, SpecificRecordBase> producer;
    private final KafkaConsumer<String, SpecificRecordBase> consumer;
    private final SimilarityService similarityService;

    public void start() {

        // регистрируем хук, который при штатном завершении работы вызовет wakeup, сгенерит WakeupException -> отработает finally
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {

            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(kafkaProps.getConsumer().getPollTimeout());

                int count = 0;
                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    handleRecord(record);
                    manageOffsets(record, count);
                    count++;
                }

                // фиксируем офсет ПОСЛЕ обработки (и промежуточно) = at-least-once (т.к.даже повторная обработка не приведет к увеличению веса события)
                consumer.commitAsync((offsets, exception) -> {
                    if (exception != null) {
                        log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                    }
                });
            }

        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
            // Это блок для корректного завершения работы приложения (вызов wakeup, который генерит WakeupException)
        } catch (Exception e) {
            log.error("Ошибка во время обработки чтения сообщений из брокера", e);
        } finally {

            try {
                producer.flush(); // сбрасываем данные в буфере

                if (!currentOffsets.isEmpty()) {
                    consumer.commitSync(currentOffsets); // тут синхронно, чтобы убедиться, что все оффсеты зафиксированы.
                }
            } catch (Exception e) {
                log.error("Ошибка flush/commit при закрытии продюсера", e);
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }

    private void manageOffsets(ConsumerRecord<String, SpecificRecordBase> record, int count) {
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % 10 == 0) {
            // а тут асинхронно, чтобы не тормозить процесс
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }

    private void handleRecord(ConsumerRecord<String, SpecificRecordBase> record) {
        log.debug("топик = {}, партиция = {}, смещение = {}, значение: {}\n",
                record.topic(), record.partition(), record.offset(), record.value());

        SpecificRecordBase value = record.value();

        if (value == null) {
            log.warn("Получено пустое сообщение");
            return;
        }

        if (!(value instanceof UserActionAvro userActionAvro)) {
            log.warn("Сообщение не относится к событиям активности пользователя: {}", value.getClass().getName());
            return;
        }

        similarityService.processUserAction(userActionAvro);
    }

}
