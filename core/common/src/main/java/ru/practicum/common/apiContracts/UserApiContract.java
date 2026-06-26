package ru.practicum.common.apiContracts;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.dto.users.UserDto;
import ru.practicum.common.dto.users.UserShortDto;

import java.util.List;

public interface UserApiContract {
    @GetMapping
    List<UserDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(required = false, defaultValue = "0") @Min(0) int from,
            @RequestParam(required = false, defaultValue = "10") @Positive int size
    );

    @GetMapping("/{userId}")
    UserShortDto getUserById(@PathVariable long userId);
}
