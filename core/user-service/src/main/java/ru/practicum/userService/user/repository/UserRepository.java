package ru.practicum.userService.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.userService.user.model.User;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
}
