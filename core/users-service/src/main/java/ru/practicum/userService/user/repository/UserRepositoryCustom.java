package ru.practicum.userService.user.repository;

import ru.practicum.ewm.user.dto.UserDto;

import java.util.List;

public interface UserRepositoryCustom {

    List<UserDto> findUsers(List<Long> ids, int from, int size);
}
