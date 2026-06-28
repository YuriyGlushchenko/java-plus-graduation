package ru.practicum.common.apiContracts;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.common.dto.users.UserDto;
import ru.practicum.common.dto.users.UserShortDto;

import java.util.List;
import java.util.Map;

public interface UserApiContract {

    @GetMapping("/api/users/{userId}")
    UserDto getUserById(@PathVariable long userId);

    @GetMapping("/api/users/{userId}/short")
    UserShortDto getUserShortById(@PathVariable long userId);

    @GetMapping("/api/users/{userId}/exist")
    Boolean isUserExist(@PathVariable long userId);

    @PostMapping("/api/users/batch")
    public Map<Long, UserShortDto> getUsersShortByIds(@RequestBody List<Long> userIds);
}
