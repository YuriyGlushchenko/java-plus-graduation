package ru.practicum.stats.server.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.stat.dto.ViewStatsDto;
import ru.practicum.stats.server.model.QEndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class StatsRepositoryCustomImpl implements StatsRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<ViewStatsDto> getStats(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            boolean unique
    ) {
        QEndpointHit hit = QEndpointHit.endpointHit;

        BooleanBuilder predicate = new BooleanBuilder();

        predicate.and(hit.timestamp.between(start, end));

        if (uris != null && !uris.isEmpty()) {
            predicate.and(hit.uri.in(uris));
        }

        NumberExpression<Long> hitsExpression = unique
                ? hit.ip.countDistinct()
                : hit.id.count();

        return queryFactory
                .select(Projections.constructor(
                        ViewStatsDto.class,
                        hit.app,
                        hit.uri,
                        hitsExpression
                ))
                .from(hit)
                .where(predicate)
                .groupBy(hit.app, hit.uri)
                .orderBy(hitsExpression.desc())
                .fetch();
    }
}
