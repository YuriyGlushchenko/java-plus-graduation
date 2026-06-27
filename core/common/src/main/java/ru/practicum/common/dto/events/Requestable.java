package ru.practicum.common.dto.events;

public interface Requestable {
    Long getId();
    void setConfirmedRequests(Long count);
}
