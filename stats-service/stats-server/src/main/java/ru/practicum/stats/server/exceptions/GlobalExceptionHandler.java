package ru.practicum.stats.server.exceptions;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.stats.server.exceptions.responseMessage.ApiError;
import ru.practicum.stats.server.exceptions.responseMessage.ValidationError;
import ru.practicum.stats.server.exceptions.responseMessage.ValidationErrorResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    // Перехват исключения при валидации аргументов тела запроса с @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {

        // Получаем все ошибки валидации полей
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        List<ValidationError> validationErrors = fieldErrors.stream()
                .map(fieldError -> new ValidationError(
                        fieldError.getField(),
                        fieldError.getDefaultMessage(),
                        fieldError.getRejectedValue()
                ))
                .collect(Collectors.toList());

        return new ValidationErrorResponse("VALIDATION_FAILED", validationErrors);
    }

    // перехват исключений, при ошибках валидации с кастомными аннотациями или ручной валидации
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleValidationException(ValidationException ex) {

        List<ValidationError> validationErrors = List.of(
                new ValidationError(ex.getFieldName(), ex.getMessage(), ex.getRejectedValue()));

        return new ValidationErrorResponse("VALIDATION_FAILED", validationErrors);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(final Exception ex, HttpStatus status) {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String stackTrace = sw.toString();

        return new ApiError(status, ex.getMessage(), stackTrace);
    }

}

