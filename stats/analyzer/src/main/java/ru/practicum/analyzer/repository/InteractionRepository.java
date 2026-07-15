package ru.practicum.analyzer.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.analyzer.model.Interaction;
import ru.practicum.analyzer.model.RecommendedEventDto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    // Получить последние N взаимодействий пользователя (для рекомендаций)
    @Query("SELECT i FROM Interaction i WHERE i.userId = :userId ORDER BY i.timestamp DESC")
    List<Interaction> findTopNByUserId(@Param("userId") Long userId, Pageable pageable);

    // Получить все мероприятия, с которыми взаимодействовал пользователь
    @Query("SELECT i.eventId FROM Interaction i WHERE i.userId = :userId")
    List<Long> findEventIdsByUserId(@Param("userId") Long userId);

    // Получить рейтинг пользователя для мероприятия
    Optional<Interaction> findByUserIdAndEventId(Long userId, Long eventId);

    @Query("""
            SELECT
                new ru.practicum.analyzer.model.RecommendedEventDto(
                    i.eventId,
                    SUM(i.weight)
                )
            FROM Interaction i
            WHERE i.eventId IN :eventIds
            GROUP BY i.eventId
            """)
    List<RecommendedEventDto> findTotalWeightsByEventIds(@Param("eventIds") Collection<Long> eventIds);

}