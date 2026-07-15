package ru.practicum.eventsService.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.common.dto.comments.CommentStatus;
import ru.practicum.eventsService.client.CommentClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CommentClientFallbackFactory implements FallbackFactory<CommentClient> {

    @Override
    public CommentClient create(Throwable cause) {
        return new CommentClient() {

            @Override
            public Map<Long, Long> getCommentCountsByEventIds(List<Long> eventIds, CommentStatus status) {
                log.error("Comment service unavailable for eventIds={}", eventIds, cause);

                Map<Long, Long> fallbackMap = new HashMap<>();
                eventIds.forEach(id -> fallbackMap.put(id, -1L));
                return fallbackMap;
            }
        };
    }
}