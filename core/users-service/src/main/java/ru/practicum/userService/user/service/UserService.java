package ru.practicum.userService.user.service;



import ru.practicum.common.dto.users.UserDto;
import ru.practicum.common.dto.users.UserShortDto;
import ru.practicum.userService.user.dto.NewUserRequest;

import java.util.List;
import java.util.Map;

public interface UserService {

    UserDto createUser(NewUserRequest newUserRequest);

    List<UserDto> findUsers(List<Long> ids, int from, int size);

    void deleteUser(Long userId);

    UserDto findUser(Long userId);

    Boolean isUserExist(Long userId);

    UserShortDto findUserShort(Long userId);

    Map<Long, UserShortDto> getUsersShortByIds(List<Long> userIds);
}
