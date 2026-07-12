package ru.practicum.clients.analyzer;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.clients.dto.RecommendedEventDto;
import ru.practicum.recommendations.proto.*;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

@Slf4j
@Component
public class AnalyzerGrpcClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub client;

    /**
     * Получить рекомендации для пользователя.
     */
    public List<RecommendedEventDto> getRecommendationsForUser(long userId, int maxResults) {
        try {
            UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();

            return toDtoList(client.getRecommendationsForUser(request));

        } catch (StatusRuntimeException e) {
            log.error("Failed to get recommendations for user {}", userId, e);
            throw new RuntimeException("Failed to get recommendations from Analyzer", e);
        }
    }

    /**
     * Получить похожие мероприятия.
     */
    public List<RecommendedEventDto> getSimilarEvents(long eventId, long userId, int maxResults) {
        try {
            SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                    .setEventId(eventId)
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();

            return toDtoList(client.getSimilarEvents(request));

        } catch (StatusRuntimeException e) {
            log.error("Failed to get similar events for event {}", eventId, e);
            throw new RuntimeException("Failed to get similar events from Analyzer", e);
        }
    }

    /**
     * Получить рейтинг мероприятий.
     */
    public List<RecommendedEventDto> getInteractionsCount(List<Long> eventIds) {
        try {
            InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                    .addAllEventIds(eventIds)
                    .build();

            return toDtoList(client.getInteractionsCount(request));

        } catch (StatusRuntimeException e) {
            log.error("Failed to get interactions count", e);
            throw new RuntimeException("Failed to get interactions count from Analyzer", e);
        }
    }

    /**
     * Преобразовать gRPC-ответ в список DTO.
     */
    private List<RecommendedEventDto> toDtoList(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                        false)
                .map(this::toDto)
                .toList();
    }

    /**
     * Преобразовать protobuf в DTO.
     */
    private RecommendedEventDto toDto(RecommendedEventProto proto) {
        return RecommendedEventDto.builder()
                .eventId(proto.getEventId())
                .score(proto.getScore())
                .build();
    }
}