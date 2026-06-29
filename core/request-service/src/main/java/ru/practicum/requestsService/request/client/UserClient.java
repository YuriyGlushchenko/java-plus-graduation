package ru.practicum.requestsService.request.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.common.apiContracts.UserApiContract;

@FeignClient(name = "user-service")
public interface UserClient extends UserApiContract {
}
