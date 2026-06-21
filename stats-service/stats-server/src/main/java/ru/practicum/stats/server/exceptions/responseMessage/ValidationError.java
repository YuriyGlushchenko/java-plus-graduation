package ru.practicum.stats.server.exceptions.responseMessage;

import lombok.Data;

@Data
public class ValidationError {
    private String fieldName;
    private String message;
    private Object rejectedValue;

    public ValidationError(String fieldName, String message, Object rejectedValue) {
        this.fieldName = fieldName;
        this.message = message;
        this.rejectedValue = rejectedValue;
    }
}