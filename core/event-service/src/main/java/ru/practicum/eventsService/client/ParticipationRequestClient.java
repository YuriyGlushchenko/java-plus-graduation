package ru.practicum.eventsService.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.common.apiContracts.ParticipationRequestApiContract;

@FeignClient(name = "requests-service")
public interface ParticipationRequestClient extends ParticipationRequestApiContract {
}
