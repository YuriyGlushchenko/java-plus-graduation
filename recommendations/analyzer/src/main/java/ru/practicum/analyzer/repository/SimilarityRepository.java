package ru.practicum.analyzer.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.analyzer.model.Similarity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SimilarityRepository extends JpaRepository<Similarity, Long> {
    // 1. Получить все сходства для мероприятия (по event1 или event2)
    @Query("SELECT s FROM Similarity s WHERE s.event1 = :eventId OR s.event2 = :eventId ORDER BY s.similarity DESC")
    List<Similarity> findByEventId(@Param("eventId") Long eventId);

    // 2. Получить топ-N похожих мероприятий
    @Query("SELECT s FROM Similarity s WHERE s.event1 = :eventId OR s.event2 = :eventId ORDER BY s.similarity DESC")
    List<Similarity> findTopNByEventId(@Param("eventId") Long eventId, Pageable pageable);

    // 3. Получить сходство между двумя мероприятиями
    @Query("SELECT s FROM Similarity s WHERE (s.event1 = :eventA AND s.event2 = :eventB) OR (s.event1 = :eventB AND s.event2 = :eventA)")
    Optional<Similarity> findByEventPair(@Param("eventA") Long eventA,
                                         @Param("eventB") Long eventB);


    // 1. Основной метод для поиска всех сходств по списку ID мероприятий
    @Query("SELECT s FROM Similarity s WHERE s.event1 IN :eventIds OR s.event2 IN :eventIds ORDER BY s.similarity DESC")
    List<Similarity> findByEventIds(@Param("eventIds") Collection<Long> eventIds, Pageable pageable);



    // Запрос сразу с ограничением на количество записей для каждого из id. Альтернатива - запросы в цикле, но будет N+1
    @Query(value = """
        WITH ranked AS (
            SELECT 
                s.id,
                s.event1,
                s.event2,
                s.similarity,
                s.timestamp,
                ROW_NUMBER() OVER (
                    PARTITION BY 
                        CASE 
                            WHEN s.event1 = ANY(:eventIds) THEN s.event1
                            ELSE s.event2
                        END
                    ORDER BY s.similarity DESC
                ) as rn
            FROM similarities s
            WHERE s.event1 = ANY(:eventIds) OR s.event2 = ANY(:eventIds)
        )
        SELECT 
            ranked.id,
            ranked.event1,
            ranked.event2,
            ranked.similarity,
            ranked.timestamp
        FROM ranked 
        WHERE rn <= :limit
        """, nativeQuery = true)
    List<Similarity> findTopNByEventIds(@Param("eventIds") List<Long> eventIds,
                                        @Param("limit") int limit);
}
