package ru.practicum.common.dto.participationRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;  // список id запросов
    private RequestStatus status;    // CONFIRMED / REJECTED
}