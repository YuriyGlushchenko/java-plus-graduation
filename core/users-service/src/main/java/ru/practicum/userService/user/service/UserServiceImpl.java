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
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Loggable
    public UserDto findUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new NotFoundException("User with id: " + userId + " not found");
        }
        return UserMapper.toUserDto(userOptional.get());
    }

    @Override
    public Boolean isUserExist(Long userId) {
        return userRepository.existsById(userId);
    }

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

    // toDo удалить в конце

    //    public List<UserDto> findBy(UserParam param){
//        Specification<User> spec = new UserSpecification(param);
//        Page<User> page = userRepository.findAll(spec, param.getPageable());
//        HashMap<User, Integer> views = new HashMap<>(); // через клиента
//        List<UserDto> dto = userMapper.toDto(page.getContent(), views);
//        return dto;
//    }


}
