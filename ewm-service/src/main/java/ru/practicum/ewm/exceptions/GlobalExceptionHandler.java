package ru.practicum.ewm.exceptions;


import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.ewm.exceptions.exceptions.ConditionsNotMetException;
import ru.practicum.ewm.exceptions.exceptions.NotFoundException;
import ru.practicum.ewm.exceptions.exceptions.ValidationException;
import ru.practicum.ewm.exceptions.responseMessage.ApiError;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // отлов ошибок валидации из контроллера от параметров с аннотацией @RequestBody @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());

        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        String message = fieldErrors.stream()
                .map(error -> String.format("Field: %s. Error: %s. Value: %s",
                        error.getField(),
                        error.getDefaultMessage(),
                        error.getRejectedValue()))
                .collect(Collectors.joining("; "));

        return ApiError.builder()
                .errors(Collections.emptyList()) // в спецификации в примерах нет стектрейсов, а в schemas есть
                .message(message)
                .reason("Incorrectly made request.")
                .status(HttpStatus.BAD_REQUEST.name())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    // обработка ValidationException в кастомынх аннотациях - пока их нет, но потом может добавим, смотря что там по заданию
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(ValidationException ex) {
        log.warn("Validation exception: {}", ex.getMessage());

        String message = String.format("Field: %s. Error: %s. Value: %s",
                ex.getFieldName(), ex.getMessage(), ex.getRejectedValue());

        return ApiError.builder()
                .errors(Collections.emptyList())
                .message(message)
                .reason("Incorrectly made request.")
                .status(HttpStatus.BAD_REQUEST.name())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    // Обработка ограничений в SQL, например UNIQUE на поле email у юзера.
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage());

        return ApiError.builder()
                .errors(Collections.emptyList())
                .message(ex.getMessage())
                .reason("Integrity constraint has been violated.")
                .status(HttpStatus.CONFLICT.name())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    // отлавливаем ошибки валидации @RequestParam параметров в методах контроллера
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());

        // Собираем все в одну строку, т.к. там может быть много нарушений сразу
        String message = ex.getConstraintViolations().stream()
                .map(violation -> {
                    String paramName = violation.getPropertyPath().toString();
                    return String.format("Field: %s. Error: %s. Value: %s",
                            paramName,
                            violation.getMessage(),
                            violation.getInvalidValue());
                })
                .collect(Collectors.joining("; "));

        return ApiError.builder()
                .errors(Collections.emptyList())
                .message(message)
                .reason("Incorrectly made request.")
                .status(HttpStatus.BAD_REQUEST.name())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    // отлов ошибок, когда в @PathVariable приходят некорректные данные, которые спринг не может преобразовать (текст вместо числа)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Type mismatch: {}", ex.getMessage());
        return ApiError.builder()
                .errors(Collections.emptyList())
                .message(ex.getMessage())
                .reason("Incorrectly made request.")
                .status(HttpStatus.BAD_REQUEST.name())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    // сущность не найдена
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException ex) {
        log.info("Resource not found: {}", ex.getMessage());

        return ApiError.builder()
                .errors(Collections.emptyList())
                .message(ex.getMessage())
                .reason("The required object was not found.")
                .status(HttpStatus.NOT_FOUND.name())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    @ExceptionHandler(ConditionsNotMetException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConditionsNotMetException(ConditionsNotMetException ex) {
        log.warn("Conditions not met: {}", ex.getMessage());
        return ApiError.builder()
                .errors(Collections.emptyList())
                .message(ex.getMessage())
                .reason("For the requested operation the conditions are not met.")
                .status(HttpStatus.FORBIDDEN.name())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    //  обработка ошибок, когда не передан обязательный @RequestParam в контроллере
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        log.warn("Missing request parameter: {}", ex.getMessage());
        return ApiError.builder()
                .errors(Collections.emptyList())
                .message(ex.getMessage())
                .reason("Incorrectly made request.")
                .status(HttpStatus.BAD_REQUEST.name())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }


    // Обработка всех остальных исключений (500), как было в вебинаре
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(Exception ex) {
        log.error("Internal server error", ex);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String stackTrace = sw.toString();

        return ApiError.builder()
                .errors(Collections.singletonList(stackTrace)) // создаёт неизменяемый список, содержащий ровно один элемент
                .message(ex.getMessage())
                .reason("Internal server error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }
}

