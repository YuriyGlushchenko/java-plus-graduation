package ru.practicum.eventsService.client.fallback;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.common.dto.users.UserShortDto;
import ru.practicum.eventsService.client.UserClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        return new UserClient() {

            @Override
            public UserShortDto getUserShortById(long userId) {
                // 404 — пользователь не найден — возвращаем null
                if (cause instanceof FeignException.NotFound) {
                    log.debug("User not found: userId={}", userId);
                    return null;
                }

                // другие ошибки (500)
                log.error("User service error for userId={}: {}", userId, cause.getMessage());
                return new UserShortDto(userId, "Unavailable");
            }

            @Override
            public Map<Long, UserShortDto> getUsersDataByIds(List<Long> userIds) {
                log.error("User service error for batch request: {}", cause.getMessage());

                // Возвращаем заглушки "Unavailable" для всех запрошенных ID
                return userIds.stream()
                        .distinct()
                        .collect(Collectors.toMap(
                                id -> id,
                                id -> new UserShortDto(id, "Unavailable")
                        ));
            }
        };
    }
}