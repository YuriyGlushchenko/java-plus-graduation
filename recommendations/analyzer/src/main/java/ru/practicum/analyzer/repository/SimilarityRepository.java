package ru.practicum.analyzer.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.analyzer.model.Similarity;
import ru.practicum.analyzer.repository.projection.NeighborProjection;
import ru.practicum.analyzer.repository.projection.RecommendedEventProjection;

import java.util.Collection;
import java.util.List;

public interface SimilarityRepository extends JpaRepository<Similarity, Long> {
    // Получить все сходства для заданного мероприятия (по event1 или event2)
    @Query("SELECT s FROM Similarity s WHERE s.event1 = :eventId OR s.event2 = :eventId ORDER BY s.similarity DESC")
    List<Similarity> findByEventId(@Param("eventId") Long eventId);

    // Получить топ-N похожих мероприятий на одно данное с eventId
    @Query("SELECT s FROM Similarity s WHERE s.event1 = :eventId OR s.event2 = :eventId ORDER BY s.similarity DESC")
    List<Similarity> findTopNByEventId(@Param("eventId") Long eventId, Pageable pageable);

    // Метод возвращает список мероприятий (проекция для id и score), которые похожи на мероприятия из переданного списка eventIds.
    // При этом, сразу отсекаются события, в которых уже отметился пользователь с userId
    @Query(value = """
            SELECT
                CASE
                    WHEN s.event1 IN (:eventIds) THEN s.event2
                    ELSE s.event1
                END AS eventId,
                MAX(s.similarity) AS similarity
            FROM similarities s
            WHERE (
                    s.event1 IN (:eventIds)
                    AND s.event2 NOT IN (
                        SELECT event_id
                        FROM interactions
                        WHERE user_id = :userId
                    )
                )
               OR (
                    s.event2 IN (:eventIds)
                    AND s.event1 NOT IN (
                        SELECT event_id
                        FROM interactions
                        WHERE user_id = :userId
                    )
                )
            GROUP BY eventId
            ORDER BY s.similarity DESC,
                     eventId
            LIMIT :limit
            """, nativeQuery = true)
    List<RecommendedEventProjection> findRecommendedEvents(@Param("eventIds") Collection<Long> eventIds,
                                                           @Param("userId") Long userId,
                                                           @Param("limit") int limit);

    // "Получить K наиболее похожих мероприятий, с которыми пользователь уже взаимодействовал" - ищем соседей по подобию
    @Query(value = """
            SELECT
                CASE
                    WHEN s.event1 = :candidateEvent THEN s.event2
                    ELSE s.event1
                END AS eventId,
                s.similarity AS similarity
            FROM similarities s
            WHERE (
                    s.event1 = :candidateEvent
                    AND s.event2 IN (:userEvents)
                )
               OR (
                    s.event2 = :candidateEvent
                    AND s.event1 IN (:userEvents)
                )
            ORDER BY s.similarity DESC,
                     eventId
            LIMIT :limit
            """, nativeQuery = true)
    List<NeighborProjection> findNearestNeighbors(@Param("candidateEvent") Long candidateEvent,
                                                  @Param("userEvents") Collection<Long> userEvents,
                                                  @Param("limit") int limit);
}
