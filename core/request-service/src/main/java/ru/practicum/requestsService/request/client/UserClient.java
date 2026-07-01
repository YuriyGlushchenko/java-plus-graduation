package ru.practicum.requestsService.request.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.common.apiContracts.UserApiContract;
import ru.practicum.requestsService.request.client.fallback.UserClientFallbackFactory;

@FeignClient(name = "user-service", fallbackFactory = UserClientFallbackFactory.class)
public interface UserClient extends UserApiContract {
}
