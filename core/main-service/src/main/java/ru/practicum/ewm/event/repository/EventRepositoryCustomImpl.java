package ru.practicum.ewm.event.repository;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.categories.dto.CategoryDto;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.paramDto.EventRepositoryParam;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.QEvent;
import ru.practicum.ewm.request.model.QParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EventRepositoryCustomImpl implements EventRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QEvent event = QEvent.event;
    private final QParticipationRequest request = QParticipationRequest.participationRequest;

    @Override
    public List<EventShortDto> findEventsShortDto(EventRepositoryParam param) {

        BooleanBuilder predicate = createPredicate(param);

        // начинаем составлять запрос (но не отправляем!)
        var query = queryFactory  // var - компилятор сам определяет тип (JPAQuery<EventShortDto> в данном случае)
                .select(Projections.constructor(EventShortDto.class,
                        event.id,
                        event.annotation,
                        Projections.constructor(CategoryDto.class, event.category.id, event.category.name), // проекция в DTO
                        request.count().as("confirmedRequests"),
                        event.eventDate,
                        Projections.constructor(UserShortDto.class, event.initiator.id, event.initiator.name), // проекция в DTO
                        event.paid,
                        event.publishedOn,
                        event.title,
                        Expressions.asNumber(0L).as("views"),
                        Expressions.asNumber(0L).as("commentsCount")
                ))
                .from(event)
                .leftJoin(request).on(
                        request.event.eq(event)
                                .and(request.status.eq(RequestStatus.CONFIRMED))
                )
                .where(predicate)
                .groupBy(
                        event.id, // нужен только .groupBy(event.id), но для postgres обязательно все поля перечислять из select
                        event.category.id,
                        event.category.name,
                        event.initiator.id,
                        event.initiator.name,
                        event.paid,
                        event.title
                );

        // добавляем фильтрацию в запрос, если требуются только доступные события
        if (param.isOnlyAvailable()) {
            query.having(
                    event.participantLimit.eq(0)
                            .or(request.count().lt(event.participantLimit))
            );
        }

        return query
                .orderBy(event.eventDate.asc()) // сортируем сразу по дате, если нужна по views, то потом в сервисе переделываем
                .offset(param.getFrom())
                .limit(param.getSize())
                .fetch(); // вот только тут отправляется запрос

    }


    @Override
    public Optional<EventFullDto> findEventByIdFullDto(Long id) {

        return Optional.ofNullable(
                queryFactory
                        .select(Projections.constructor(EventFullDto.class,
                                event.id,
                                event.annotation,
                                Projections.constructor(CategoryDto.class, // Категория: создаём CategoryDto через проекцию
                                        event.category.id,
                                        event.category.name
                                ),
                                request.count().as("confirmedRequests"),
                                event.createdOn,
                                event.description,
                                event.eventDate,
                                //  Инициатор c типом User -> создаём UserShortDto через проекцию
                                Projections.constructor(UserShortDto.class, event.initiator.id, event.initiator.name),
                                event.location, // в Event это @Embedded поле Location, а в таблице две колонки lat и lon
                                event.paid,
                                event.participantLimit,
                                event.publishedOn,
                                event.requestModeration,
                                event.state,
                                event.title,
                                Expressions.asNumber(0L).as("views"), // пока 0, потом подгружаем в сервисе
                                Expressions.asNumber(0L).as("commentsCount")
                        ))
                        .from(event)
                        .leftJoin(request).on(request.event.eq(event).and(request.status.eq(RequestStatus.CONFIRMED)))
                        .where(event.id.eq(id))
                        .groupBy(
                                event.id, // нужен только .groupBy(event.id), но для postgres обязательно все поля перечислять из select
                                event.category.id,
                                event.category.name,
                                event.initiator.id,
                                event.initiator.name,
                                event.location,
                                event.paid,
                                event.participantLimit,
                                event.publishedOn,
                                event.requestModeration,
                                event.state,
                                event.title
                        )
                        .fetchOne()
        );
    }

    @Override
    public List<EventFullDto> findEventsFullDto(EventRepositoryParam param) {

        BooleanBuilder predicate = createPredicate(param);

        var query = queryFactory
                .select(Projections.constructor(EventFullDto.class,
                        event.id,
                        event.annotation,
                        Projections.constructor(CategoryDto.class,
                                event.category.id,
                                event.category.name
                        ),
                        request.count().as("confirmedRequests"),
                        event.createdOn,
                        event.description,
                        event.eventDate,
                        Projections.constructor(UserShortDto.class,
                                event.initiator.id,
                                event.initiator.name
                        ),
                        event.location,
                        event.paid,
                        event.participantLimit,
                        event.publishedOn,
                        event.requestModeration,
                        event.state,
                        event.title,
                        Expressions.asNumber(0L).as("views"),
                        Expressions.asNumber(0L).as("commentsCount")
                ))
                .from(event)
                .leftJoin(request).on(
                        request.event.eq(event)
                                .and(request.status.eq(RequestStatus.CONFIRMED))
                )
                .where(predicate)
                .groupBy(
                        event.id, // нужен только .groupBy(event.id), но для postgres обязательно все поля перечислять из select
                        event.category.id,
                        event.category.name,
                        event.initiator.id,
                        event.initiator.name,
                        event.location,
                        event.paid,
                        event.participantLimit,
                        event.publishedOn,
                        event.requestModeration,
                        event.state,
                        event.title
                );

        if (param.isOnlyAvailable()) {
            query.having(
                    event.participantLimit.eq(0)
                            .or(request.count().lt(event.participantLimit))
            );
        }

        return query
                .orderBy(event.eventDate.asc())
                .offset(param.getFrom())
                .limit(param.getSize())
                .fetch();
    }

    private BooleanBuilder createPredicate(EventRepositoryParam param) {
        BooleanBuilder predicate = new BooleanBuilder();

        if (param.isPublicRequest()) {  // для публичных запросов только PUBLISHED события
            predicate.and(event.state.eq(EventState.PUBLISHED));
        } else if (param.hasStates()) {  // для админских запросов добавляем states в предикат, если они переданы
            predicate.and(event.state.in(param.getStates()));
        }

        if (param.hasTextSearchRequest()) {
            predicate.and(
                    event.annotation.containsIgnoreCase(param.getText())
                            .or(event.description.containsIgnoreCase(param.getText())));
        }

        if (param.hasCategories()) {
            predicate.and(event.category.id.in(param.getCategories()));
        }

        if (param.hasPaidParam()) {
            predicate.and(event.paid.eq(param.getPaid()));
        }

        if (param.hasUsers()) {
            predicate.and(event.initiator.id.in(param.getUsers()));
        }

        if (param.hasIds()) {
            predicate.and(event.id.in(param.getIds()));
        }

        if (param.hasDateRange()) {
            predicate.and(event.eventDate.between(param.getRangeStart(), param.getRangeEnd()));
        } else if (param.hasRangeStart()) { // прямо не указано как обрабатывать, когда одна граница
            predicate.and(event.eventDate.after(param.getRangeStart()));
        } else if (param.hasRangeEnd()) {
            predicate.and(event.eventDate.before(param.getRangeEnd()));
        } else {
            predicate.and(event.eventDate.after(LocalDateTime.now()));
        }

        return predicate;
    }
}