package ru.practicum.eventsService.event.dto;

public interface Commentable {
    Long getId();

    void setCommentsCount(Long count);
}