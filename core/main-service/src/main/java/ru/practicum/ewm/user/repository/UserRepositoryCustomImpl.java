package ru.practicum.ewm.user.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.model.QUser;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {
    private final JPAQueryFactory queryFactory;


    @Override
    public List<UserDto> findUsers(List<Long> ids, int from, int size) {

        QUser user = QUser.user;

        BooleanBuilder predicate = new BooleanBuilder();

        if (ids != null && !ids.isEmpty()) {
            predicate.and(user.id.in(ids));
        }


        return queryFactory.select(Projections.constructor(
                        UserDto.class,
                        user.id,
                        user.email,
                        user.name))
                .from(user)
                .where(predicate)
                .orderBy(user.id.asc()) // нужна какая-то сортировка? раз есть offset, то логично использовать сортировку
                .offset(from)
                .limit(size)
                .fetch();

    }
}
