package ru.practicum.eventsService.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.common.apiContracts.CommentsApiContract;
import ru.practicum.eventsService.client.fallback.CommentClientFallbackFactory;

@FeignClient(name = "comment-service", fallbackFactory = CommentClientFallbackFactory.class)
public interface CommentClient extends CommentsApiContract {
}
