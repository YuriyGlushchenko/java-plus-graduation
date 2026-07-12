package ru.practicum.eventsService.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.common.apiContracts.UserApiContract;
import ru.practicum.eventsService.client.fallback.UserClientFallbackFactory;

@FeignClient(name = "user-service", fallbackFactory = UserClientFallbackFactory.class)
public interface UserClient extends UserApiContract {
}
