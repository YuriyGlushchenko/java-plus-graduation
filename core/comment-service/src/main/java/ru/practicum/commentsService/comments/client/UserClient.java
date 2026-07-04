package ru.practicum.commentsService.comments.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.commentsService.comments.client.fallback.UserClientFallbackFactory;
import ru.practicum.common.apiContracts.UserApiContract;

@FeignClient(name = "user-service", fallbackFactory = UserClientFallbackFactory.class)
public interface UserClient extends UserApiContract {
}
