package ru.practicum.commentsService.comments.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.common.apiContracts.EventApiContract;

@FeignClient(name = "event-service")
public interface EventClient extends EventApiContract {
}
