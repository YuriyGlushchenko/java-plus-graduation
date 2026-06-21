package ru.practicum.stats.server.exceptions.responseMessage;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ValidationErrorResponse {
    private String error;
    private List<ValidationError> validationErrorList;
    private Date timestamp;

    public ValidationErrorResponse(String errorCode, List<ValidationError> validationErrorsList) {
        this.error = errorCode;
        this.validationErrorList = validationErrorsList;
        this.timestamp = new Date();
    }

}