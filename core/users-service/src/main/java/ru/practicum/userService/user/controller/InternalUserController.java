package ru.practicum.userService.user.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.dto.users.UserDto;
import ru.practicum.common.dto.users.UserShortDto;
import ru.practicum.userService.user.service.UserService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class InternalUserController implements ru.practicum.common.apiContracts.UserApiContract {
    private final UserService userService;

    @Override
    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable long userId) {
        log.debug("Request to get user: id={}", userId);

        UserDto user = userService.findUser(userId);

        log.debug("Found user: id={}, name={}", user.getId(), user.getName());
        return user;
    }

    @Override
    @GetMapping("/{userId}/exist")
    public Boolean isUserExist(@PathVariable long userId) {
        log.debug("Request  user exist: id={}", userId);

        Boolean isExist = userService.isUserExist(userId);

        log.debug("User confirmed: id={}, exist={}", userId, isExist);
        return isExist;
    }

    @Override
    @GetMapping("/{userId}/short")
    public UserShortDto getUserShortById(@PathVariable long userId) {
        log.debug("Request to get user short info: id={}", userId);

        UserShortDto user = userService.findUserShort(userId);

        log.debug("Found user: id={}, name={}", user.getId(), user.getName());
        return user;
    }

    @PostMapping("/batch")
    public Map<Long, UserShortDto> getUsersShortByIds(@RequestBody List<Long> userIds) {
        log.debug("Internal request: get users short by ids: {}", userIds);
        return userService.getUsersShortByIds(userIds);
    }
}
