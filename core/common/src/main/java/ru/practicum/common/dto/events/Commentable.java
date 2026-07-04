package ru.practicum.common.dto.events;

public interface Commentable {
    Long getId();

    void setCommentsCount(Long count);
}