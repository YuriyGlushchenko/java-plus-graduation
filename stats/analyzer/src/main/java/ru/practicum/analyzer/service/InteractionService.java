package ru.practicum.analyzer.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.model.Interaction;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.List;

public interface InteractionService {
    @Transactional
    void updateOrSaveInteraction(UserActionAvro userActionAvro);

    List<Long> getEventsByUserId(Long userId);

    List<Interaction> getRecentInteractions(Long userId, int limit);
}
