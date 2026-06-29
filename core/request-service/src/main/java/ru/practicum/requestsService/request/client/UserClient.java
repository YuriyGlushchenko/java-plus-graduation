package ru.practicum.requestsService.request.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.common.apiContracts.UserApiContract;

@FeignClient(name = "users-service")
public interface UserClient extends UserApiContract {
}
