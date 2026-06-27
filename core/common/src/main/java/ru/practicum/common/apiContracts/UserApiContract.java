package ru.practicum.common.apiContracts;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.dto.users.UserDto;
import ru.practicum.common.dto.users.UserShortDto;

import java.util.List;
import java.util.Map;

public interface UserApiContract {

    @GetMapping("/{userId}")
    UserDto getUserById(@PathVariable long userId);

    @GetMapping("/{userId}")
    UserShortDto getUserShortById(@PathVariable long userId);

    Boolean isUserExist(@PathVariable long userId);

    @PostMapping("/batch")
    public Map<Long, UserShortDto> getUsersShortByIds(@RequestBody List<Long> userIds);
}
