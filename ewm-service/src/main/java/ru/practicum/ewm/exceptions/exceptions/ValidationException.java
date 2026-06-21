package ru.practicum.ewm.exceptions.exceptions;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {
    String fieldName;
    Object rejectedValue;

    public ValidationException(String fieldName, Object rejectedValue, String message) {
        super(message);
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
    }
}
