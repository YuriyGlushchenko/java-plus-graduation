package ru.practicum.commentsService.comments.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.common.apiContracts.UserApiContract;

@FeignClient(name = "user-service")
public interface UserClient extends UserApiContract {
}
