package ru.practicum.userService.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.aop.annotation.Loggable;
import ru.practicum.common.dto.users.UserDto;
import ru.practicum.common.dto.users.UserShortDto;
import ru.practicum.common.exceptions.exceptions.NotFoundException;
import ru.practicum.userService.user.dto.NewUserRequest;
import ru.practicum.userService.user.dto.UserMapper;
import ru.practicum.userService.user.model.User;
import ru.practicum.userService.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserShortDto findUserShort(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new NotFoundException("User with id: " + userId + " not found");
        }
        return UserMapper.toUserShortDto(userOptional.get());
    }

    @Transactional
    @Override
    @Loggable
    public UserDto createUser(NewUserRequest newUserRequest) {
        // Email не проверяется на уникальность, т.к. отлавливается DataIntegrityViolationException при нарушении UNIQUE в БД
        User user = userRepository.save(UserMapper.toUser(newUserRequest));
        log.info("Created user with id: {}", user.getId());

        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> findUsers(List<Long> ids, int from, int size) {
        List<UserDto> users = userRepository.findUsers(ids, from, size);
        log.debug("Found {} users", users.size());

        return users;
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        userRepository.deleteById(userId);
        log.info("Deleted user with id: {}", userId);
    }

    @Override
    public Map<Long, UserShortDto> getUsersShortByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        // Убираем дубликаты
        List<Long> uniqueIds = userIds.stream().distinct().collect(Collectors.toList());

        List<User> users = userRepository.findAllById(uniqueIds);

        return users.stream()
                .collect(Collectors.toMap(
                                User::getId,
                                UserMapper::toUserShortDto)
//                        user -> new UserShortDto(user.getId(), user.getName())
                );
    }


}
