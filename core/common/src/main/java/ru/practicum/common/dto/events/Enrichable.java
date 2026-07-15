package ru.practicum.common.dto.events;


import ru.practicum.common.dto.users.UserShortDto;

public interface Enrichable extends Rateable, Requestable, Commentable {
    UserShortDto getInitiator();

    void setInitiator(UserShortDto initiator);
}