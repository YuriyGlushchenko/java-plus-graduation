package ru.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.analyzer.model.Similarity;

import java.util.List;
import java.util.Optional;

public interface SimilarityRepository extends JpaRepository<Similarity, Long> {
    // 1. Получить все сходства для мероприятия (по event1 или event2)
    @Query("SELECT s FROM Similarity s WHERE s.event1 = :eventId OR s.event2 = :eventId ORDER BY s.similarity DESC")
    List<Similarity> findByEventId(@Param("eventId") Long eventId);

    // 2. Получить топ-N похожих мероприятий
    @Query("SELECT s FROM Similarity s WHERE s.event1 = :eventId OR s.event2 = :eventId ORDER BY s.similarity DESC")
    List<Similarity> findTopNByEventId(@Param("eventId") Long eventId,
                                       org.springframework.data.domain.Pageable pageable);

    // 3. Получить сходство между двумя мероприятиями
    @Query("SELECT s FROM Similarity s WHERE (s.event1 = :eventA AND s.event2 = :eventB) OR (s.event1 = :eventB AND s.event2 = :eventA)")
    Optional<Similarity> findByEventPair(@Param("eventA") Long eventA,
                                         @Param("eventB") Long eventB);
}
