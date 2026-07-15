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
import java.util.Optional;

public interface SimilarityRepository extends JpaRepository<Similarity, Long> {

    // Получить все сходства для заданного мероприятия (по event1 или event2)
    @Query("SELECT s FROM Similarity s WHERE s.event1 = :eventId OR s.event2 = :eventId ORDER BY s.similarity DESC")
    List<Similarity> findByEventId(@Param("eventId") Long eventId);

    // Получить топ-N похожих мероприятий на одно данное с eventId
    @Query("SELECT s FROM Similarity s WHERE s.event1 = :eventId OR s.event2 = :eventId ORDER BY s.similarity DESC")
    List<Similarity> findTopNByEventId(@Param("eventId") Long eventId, Pageable pageable);

    //  event1 < event2 гарантировано на уровне SQL + в сервисах проверки
    Optional<Similarity> findByEvent1AndEvent2(Long event1, Long event2);


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


    // Получить K наиболее похожих мероприятий для каждого кандидата (проекция для candidateEventId, eventId, similarity, rating).
    // Для каждого candidateEvent возвращаем не более K соседей. (используем оконную функцию ROW_NUMBER).
    // В проекции, кроме коэффициента сходства, сразу получаем оценку пользователя (rating).
    @Query(value = """
            SELECT
                t.candidateEventId,
                t.eventId,
                t.similarity,
                t.userRating
            FROM (
                SELECT
                    q.*,
                    ROW_NUMBER() OVER (
                        PARTITION BY q.candidateEventId
                        ORDER BY q.similarity DESC,
                                 q.eventId
                    ) AS rn
                FROM (
            
                    SELECT
                        s.event1 AS candidateEventId,
                        s.event2 AS eventId,
                        s.similarity,
                        i.weight AS userRating
                    FROM similarities s
                    JOIN interactions i
                        ON i.event_id = s.event2
                    WHERE s.event1 IN (:candidateEvents)
                      AND i.user_id = :userId
            
                    UNION ALL
            
                    SELECT
                        s.event2 AS candidateEventId,
                        s.event1 AS eventId,
                        s.similarity,
                        i.weight AS userRating
                    FROM similarities s
                    JOIN interactions i
                        ON i.event_id = s.event1
                    WHERE s.event2 IN (:candidateEvents)
                      AND i.user_id = :userId
            
                ) q
            ) t
            WHERE t.rn <= :limit
            ORDER BY t.candidateEventId,
                     t.similarity DESC,
                     t.eventId
            """, nativeQuery = true)
    List<NeighborProjection> findNearestNeighbors(@Param("candidateEvents") List<Long> candidateEvents,
                                                  @Param("userId") Long userId,
                                                  @Param("limit") int limit);


    /**
     * Получить топ-N похожих мероприятий для заданного eventId,
     * исключая те, с которыми пользователь уже взаимодействовал.
     */
    @Query(value = """
            SELECT
                CASE
                    WHEN s.event1 = :eventId THEN s.event2
                    ELSE s.event1
                END AS eventId,
                s.similarity AS similarity
            FROM similarities s
            WHERE (s.event1 = :eventId OR s.event2 = :eventId)
              AND (
                    (s.event1 = :eventId AND s.event2 NOT IN (
                        SELECT event_id FROM interactions WHERE user_id = :userId
                    ))
                    OR
                    (s.event2 = :eventId AND s.event1 NOT IN (
                        SELECT event_id FROM interactions WHERE user_id = :userId
                    ))
                  )
            ORDER BY s.similarity DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<RecommendedEventProjection> findTopSimilarEventsExcludingInteracted(@Param("eventId") Long eventId,
                                                                             @Param("userId") Long userId,
                                                                             @Param("limit") int limit);
}

