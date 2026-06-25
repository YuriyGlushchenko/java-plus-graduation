package ru.practicum.eventsService.exceptions.exceptions;

public class ConditionsNotMetException extends RuntimeException {
    public ConditionsNotMetException(String message) {
        super(message);
    }
}
