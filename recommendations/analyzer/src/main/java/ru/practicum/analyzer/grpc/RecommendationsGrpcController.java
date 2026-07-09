package ru.practicum.analyzer.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.analyzer.grpc.mapper.RecommendationEventMapper;
import ru.practicum.analyzer.model.RecommendedEventDto;
import ru.practicum.analyzer.service.RecommendationService;
import ru.practicum.recommendations.proto.*;

import java.util.List;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RecommendationsGrpcController
        extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationService recommendationService;
    private final RecommendationEventMapper mapper;

    @Override
    public void getRecommendationsForUser(
            UserPredictionsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver) {

        try {
            long userId = request.getUserId();
            int maxResults = request.getMaxResults();

            log.debug("Getting recommendations for user: {}, maxResults: {}", userId, maxResults);

            List<RecommendedEventDto> recommendations = recommendationService.getRecommendationsForUser(userId, maxResults);

            // асинхронная отправка потока сообщений
            recommendations.stream()
                    .map(mapper::toProto)
                    .forEach(responseObserver::onNext);

            responseObserver.onCompleted();
            log.info("Sent {} recommendations for user {}", recommendations.size(), userId);

        } catch (Exception e) {
            log.error("Error getting recommendations for user: {}", request.getUserId(), e);
            responseObserver.onError(
                    new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage()))
            );
        }
    }

    @Override
    public void getSimilarEvents(
            SimilarEventsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver) {

        try {
            long eventId = request.getEventId();
            long userId = request.getUserId();
            int maxResults = request.getMaxResults();

            log.debug("Getting similar events for event: {}, user: {}, maxResults: {}",
                    eventId, userId, maxResults);

            List<RecommendedEventDto> recommendations = recommendationService.getSimilarEvents(eventId, userId, maxResults);

            // асинхронная отправка потока сообщений
            recommendations.stream()
                    .map(mapper::toProto)
                    .forEach(responseObserver::onNext);


            responseObserver.onCompleted();
            log.info("Sent {} similar events for event {}", recommendations.size(), eventId);

        } catch (Exception e) {
            log.error("Error getting similar events for event: {}", request.getEventId(), e);
            responseObserver.onError(
                    new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage()))
            );
        }
    }

    @Override
    public void getInteractionsCount(
            InteractionsCountRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver) {

        try {
            List<Long> eventIds = request.getEventIdsList();

            log.debug("Getting interactions count for {} events", eventIds.size());

            List<RecommendedEventDto> recommendations = recommendationService.getInteractionsCount(eventIds);

            // Отправляем каждое мероприятие потоком
            recommendations.stream()
                    .map(mapper::toProto)
                    .forEach(responseObserver::onNext);

            responseObserver.onCompleted();
            log.info("Sent interactions count for {} events", recommendations.size());

        } catch (Exception e) {
            log.error("Error getting interactions count", e);
            responseObserver.onError(
                    new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage()))
            );
        }
    }
}