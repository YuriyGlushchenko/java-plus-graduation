package ru.practicum.userService.user.repository;



import ru.practicum.common.dto.users.UserDto;

import java.util.List;

public interface UserRepositoryCustom {

    List<UserDto> findUsers(List<Long> ids, int from, int size);
}
