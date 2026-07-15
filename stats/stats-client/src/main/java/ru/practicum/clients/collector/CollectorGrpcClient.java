package ru.practicum.clients.collector;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.clients.dto.UserActionType;
import ru.practicum.recommendations.proto.ActionTypeProto;
import ru.practicum.recommendations.proto.UserActionControllerGrpc;
import ru.practicum.recommendations.proto.UserActionProto;

import java.time.Instant;

@Slf4j
@Component
public class CollectorGrpcClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub client;

    /**
     * Пользователь просмотрел мероприятие.
     */
    public void view(long userId, long eventId) {
        collect(userId, eventId, UserActionType.VIEW);
    }

    /**
     * Пользователь зарегистрировался на мероприятие.
     */
    public void register(long userId, long eventId) {
        collect(userId, eventId, UserActionType.REGISTER);
    }

    /**
     * Пользователь поставил лайк мероприятию.
     */
    public void like(long userId, long eventId) {
        collect(userId, eventId, UserActionType.LIKE);
    }

    private void collect(long userId, long eventId, UserActionType actionType) {
        try {
            client.collectUserAction(buildRequest(userId, eventId, actionType));

            log.debug(
                    "Collected action {} for user {} and event {}",
                    actionType,
                    userId,
                    eventId
            );

        } catch (StatusRuntimeException e) {
            log.error(
                    "Collector is unavailable. user={}, event={}, action={}",
                    userId,
                    eventId,
                    actionType,
                    e
            );

            throw new RuntimeException("Failed to send user action to Collector", e);
        }
    }

    private UserActionProto buildRequest(long userId,
                                         long eventId,
                                         UserActionType actionType) {

        return UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(mapActionType(actionType))
                .setTimestamp(buildTimestamp())
                .build();
    }

    private ActionTypeProto mapActionType(UserActionType actionType) {
        return switch (actionType) {
            case VIEW -> ActionTypeProto.ACTION_VIEW;
            case REGISTER -> ActionTypeProto.ACTION_REGISTER;
            case LIKE -> ActionTypeProto.ACTION_LIKE;
        };
    }

    private Timestamp buildTimestamp() {
        Instant now = Instant.now();

        return Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();
    }
}