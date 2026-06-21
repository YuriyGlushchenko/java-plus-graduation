package ru.practicum.ewm.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Validated
public class AdminUserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@Valid @RequestBody NewUserRequest newUserRequest) {
        log.info("Request to create user with email: {}", newUserRequest.getEmail());

        UserDto created = userService.createUser(newUserRequest);
        log.info("User created with id: {}", created.getId());
        return created;
    }


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


}
