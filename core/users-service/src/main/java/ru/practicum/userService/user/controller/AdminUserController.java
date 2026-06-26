package ru.practicum.userService.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.dto.users.UserDto;
import ru.practicum.common.dto.users.UserShortDto;
import ru.practicum.userService.user.dto.NewUserRequest;
import ru.practicum.userService.user.service.UserService;


import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Validated
public class AdminUserController implements ru.practicum.common.apiContracts.UserApiContract {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@Valid @RequestBody NewUserRequest newUserRequest) {
        log.info("Request to create user with email: {}", newUserRequest.getEmail());

        UserDto created = userService.createUser(newUserRequest);
        log.info("User created with id: {}", created.getId());
        return created;
    }


    @Override
    @GetMapping
    public List<UserDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(required = false, defaultValue = "0") @Min(0) int from,
            @RequestParam(required = false, defaultValue = "10") @Positive int size
    ) {
        log.debug("Request to get users: ids={}, from={}, size={}", ids, from, size);

        List<UserDto> users = userService.findUsers(ids, from, size);
        log.debug("Found {} users", users.size());
        return users;
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUser(@PathVariable long userId) {
        log.info("Request to delete user with id={}", userId);

        userService.deleteUser(userId);
        log.info("User with id={} successfully deleted", userId);
    }

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

        log.debug("User confirmed: id={}, exist={}", userId,  isExist);
        return isExist;
    }

    @Override
    public UserShortDto getUserShortById(long userId) {
        log.debug("Request to get user short info: id={}", userId);

        UserShortDto user = userService.findUserShort(userId);

        log.debug("Found user: id={}, name={}", user.getId(), user.getName());
        return user;
    }
}
