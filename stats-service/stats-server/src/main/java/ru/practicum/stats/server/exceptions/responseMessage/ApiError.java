package ru.practicum.stats.server.exceptions.responseMessage;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ApiError {
    private final HttpStatus errorCode;
    private final String message;
    private final String stackTrace;

}
