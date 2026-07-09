package ru.practicum.analyzer.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.analyzer.model.Interaction;

import java.util.List;
import java.util.Optional;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    // 1. Получить последние N взаимодействий пользователя (для рекомендаций)
    @Query("SELECT i FROM Interaction i WHERE i.userId = :userId ORDER BY i.timestamp DESC")
    List<Interaction> findTopNByUserId(@Param("userId") Long userId, Pageable pageable);

    // 2. Получить все мероприятия, с которыми взаимодействовал пользователь
    @Query("SELECT i.eventId FROM Interaction i WHERE i.userId = :userId")
    List<Long> findEventIdsByUserId(@Param("userId") Long userId);

    // 3. Проверить, взаимодействовал ли пользователь с мероприятием
    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    // 4. Получить рейтинг пользователя для мероприятия
    Optional<Interaction> findByUserIdAndEventId(Long userId, Long eventId);

    // 5. Получить все взаимодействия пользователя
    List<Interaction> findAllByUserId(Long userId);

    // 6. Получить все взаимодействия для мероприятия (для расчета сходства)
    List<Interaction> findAllByEventId(Long eventId);
}