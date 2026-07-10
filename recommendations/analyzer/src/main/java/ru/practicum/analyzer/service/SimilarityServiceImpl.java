package ru.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.model.Similarity;
import ru.practicum.analyzer.repository.SimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimilarityServiceImpl implements SimilarityService {

    private final SimilarityRepository similarityRepository;

    @Transactional
    @Override
    public void saveSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        long eventA = eventSimilarityAvro.getEventA();
        long eventB = eventSimilarityAvro.getEventB();
        Double score = eventSimilarityAvro.getScore();
        Instant timestamp = eventSimilarityAvro.getTimestamp();


        // Гарантируем порядок event1 < event2
        long event1 = Math.min(eventA, eventB);
        long event2 = Math.max(eventA, eventB);

        Similarity similarity = Similarity.builder()
                .event1(event1)
                .event2(event2)
                .similarity(score)
                .timestamp(timestamp)
                .build();

        similarityRepository.save(similarity);
        log.debug("Saved similarity: {} - {}, score={}", event1, event2, score);
    }

    @Override
    public List<Similarity> getSimilaritiesForEvent(Long eventId) {
        return similarityRepository.findByEventId(eventId);
    }

    @Override
    public List<Similarity> getTopSimilarities(Long eventId, int limit) {
        return similarityRepository.findTopNByEventId(
                eventId,
                PageRequest.of(0, limit)
        );
    }
}