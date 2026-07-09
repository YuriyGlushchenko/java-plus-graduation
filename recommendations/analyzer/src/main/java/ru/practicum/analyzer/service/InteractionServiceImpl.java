package ru.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.model.ActionType;
import ru.practicum.analyzer.model.Interaction;
import ru.practicum.analyzer.processor.UserActionProcessor;
import ru.practicum.analyzer.repository.InteractionRepository;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {

    private final InteractionRepository interactionRepository;

    @Transactional
    @Override
    public void updateOrSaveInteraction(UserActionAvro userActionAvro) {
        Long userId = userActionAvro.getUserId();
        Long eventId = userActionAvro.getEventId();
        Double weight = ActionType.getWeight(userActionAvro.getActionType());
        Instant timestamp = userActionAvro.getTimestamp();


        // Ищем существующее взаимодействие
        var existing = interactionRepository.findByUserIdAndEventId(userId, eventId);

        // Если есть и новый вес меньше или равен старому — пропускаем
        if (existing.isPresent() && existing.get().getWeight() >= weight) {
            log.debug("Weight not changed: user={}, event={}, existing={}, new={}",
                    userId, eventId, existing.get().getWeight(), weight);
            return;
        }

        Interaction interaction = Interaction.builder()
                .userId(userId)
                .eventId(eventId)
                .weight(weight)
                .timestamp(timestamp)
                .build();

        interactionRepository.save(interaction);
        log.debug("Saved interaction: user={}, event={}, weight={}", userId, eventId, weight);
    }

    @Override
    public List<Long> getEventsByUserId(Long userId) {
        return interactionRepository.findEventIdsByUserId(userId);
    }

    @Override
    public List<Interaction> getRecentInteractions(Long userId, int limit) {
        return interactionRepository.findTopNByUserId(
                userId,
                org.springframework.data.domain.PageRequest.of(0, limit)
        );
    }
}