package ru.practicum.common.dto.events;


import ru.practicum.common.dto.users.UserShortDto;

public interface Enrichable extends Viewable, Requestable, Commentable {
    UserShortDto getInitiator();

    void setInitiator(UserShortDto initiator);
}