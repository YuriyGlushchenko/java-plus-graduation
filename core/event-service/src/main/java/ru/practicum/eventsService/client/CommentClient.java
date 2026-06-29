package ru.practicum.eventsService.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.common.apiContracts.CommentsApiContract;

@FeignClient(name = "comment-service")
public interface CommentClient extends CommentsApiContract {
}
