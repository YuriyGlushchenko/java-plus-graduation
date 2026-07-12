package ru.practicum.analyzer.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import ru.practicum.analyzer.config.KafkaProps;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class BaseProcessor<T extends SpecificRecordBase> implements Runnable {

    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new ConcurrentHashMap<>();
    private final KafkaProps kafkaProps;
    //    private final Producer<String, SpecificRecordBase> producer;
    private final KafkaConsumer<Long, T> consumer;

    public BaseProcessor(
            KafkaProps kafkaProps,
//            Producer<String, SpecificRecordBase> producer,
            KafkaConsumer<Long, T> consumer) {
        this.kafkaProps = kafkaProps;
//        this.producer = producer;
        this.consumer = consumer;
    }

    protected abstract void handleRecord(ConsumerRecord<Long, T> record);

    @Override
    public void run() {
        log.info("User processor started");

        // регистрируем хук, который при штатном завершении работы вызовет wakeup, сгенерит WakeupException -> отработает finally
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {

            while (true) {
//                log.info("Before poll");

                ConsumerRecords<Long, T> records = consumer.poll(kafkaProps.getConsumer().getPollTimeout());

//                log.info("[{}] After poll {}",
//                        Thread.currentThread().getName(),
//                        records.count());

                int count = 0;
                for (ConsumerRecord<Long, T> record : records) {
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
//                producer.flush(); // сбрасываем данные в буфере

                if (!currentOffsets.isEmpty()) {
                    consumer.commitSync(currentOffsets); // тут синхронно, чтобы убедиться, что все оффсеты зафиксированы.
                }
            } catch (Exception e) {
                log.error("Ошибка flush/commit при закрытии продюсера", e);
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
//                log.info("Закрываем продюсер");
//                producer.close();
            }
        }
    }

    private void manageOffsets(ConsumerRecord<Long, T> record, int count) {
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
}