package ru.practicum.collector.controller;


import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.collector.kafka.KafkaUserActionProducer;
import ru.practicum.collector.mapper.UserActionMapper;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.recommendations.proto.UserActionControllerGrpc;
import ru.practicum.recommendations.proto.UserActionProto;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class UserActionGrpcController extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final UserActionMapper userActionMapper;
    private final KafkaUserActionProducer producer;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        log.debug("Collecting user action PROTO: userId={}, eventId={}, action={}",
                request.getUserId(), request.getEventId(), request.getActionType());


        try {
            UserActionAvro userActionAvro = userActionMapper.toAvro(request);
            log.trace("Converted to AVRO user action: {}", userActionAvro);

            producer.sendUserAction(userActionAvro)
                    .thenAccept(metadata -> {

                        log.info("User action event sent: userId={}, eventId={}, action={}, offset={}",
                                userActionAvro.getUserId(), userActionAvro.getEventId(), userActionAvro.getActionType(), metadata.offset());

                        responseObserver.onNext(Empty.getDefaultInstance());
                        responseObserver.onCompleted();
                    })
                    .exceptionally(exception -> {
                        log.error("Failed to send action event", exception);
                        responseObserver.onError(
                                new StatusRuntimeException(Status.INTERNAL.withCause(exception))
                        );
                        return null;
                    });

        } catch (Exception e) {
            responseObserver.onError(
                    new StatusRuntimeException(Status.INVALID_ARGUMENT.withCause(e))
            );
        }

    }
}
