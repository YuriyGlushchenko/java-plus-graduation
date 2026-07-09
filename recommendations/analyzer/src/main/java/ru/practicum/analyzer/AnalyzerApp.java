package ru.practicum.analyzer;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import ru.practicum.analyzer.config.KafkaProps;
import ru.practicum.analyzer.processor.SimilarityProcessor;
import ru.practicum.analyzer.processor.UserActionProcessor;

@SpringBootApplication
@EnableConfigurationProperties(KafkaProps.class)
public class AnalyzerApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                SpringApplication.run(AnalyzerApp.class, args);

        final SimilarityProcessor similarityProcessor = context.getBean(SimilarityProcessor.class);
        UserActionProcessor userActionProcessor = context.getBean(UserActionProcessor.class);

        // запускаем в отдельных потоках обработчики сообщений из топиков
        Thread similarityThread = new Thread(similarityProcessor);
        similarityThread.setName("SimilarityHandlerThread");
        similarityThread.start();


        Thread userActionThread = new Thread(userActionProcessor);
        userActionThread.setName("UserActionHandlerThread");
        userActionThread.start();

    }
}
