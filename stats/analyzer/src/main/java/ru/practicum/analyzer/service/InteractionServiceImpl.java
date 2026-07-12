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
import java.util.Optional;

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
        Optional<Interaction> existing = interactionRepository.findByUserIdAndEventId(userId, eventId);

        if (existing.isPresent()) {
            Interaction interaction = existing.get();

            if(weight > interaction.getWeight()){
                interaction.setWeight(weight);
                log.trace("Weight updated: user={}, event={}, weight={}",
                        userId, eventId, weight);
            } else {
                log.trace("Weight not updated: user={}, event={}, weight={}",
                        userId, eventId, weight);
            }
            interaction.setTimestamp(timestamp);

            interactionRepository.save(interaction);

            log.debug("Updated interaction: user={}, event={}, weight={}",
                    userId, eventId, weight);
        } else {
            Interaction interaction = Interaction.builder()
                    .userId(userId)
                    .eventId(eventId)
                    .weight(weight)
                    .timestamp(timestamp)
                    .build();

            interactionRepository.save(interaction);

            log.debug("Created interaction: user={}, event={}, weight={}",
                    userId, eventId, weight);
        }
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