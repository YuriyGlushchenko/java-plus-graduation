package ru.practicum.ewm.event.dto.paramDto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, DateRangeValidatable> {

    @Override
    public boolean isValid(DateRangeValidatable value, ConstraintValidatorContext context) {
        if (value == null) return true;

        LocalDateTime start = value.getRangeStart();
        LocalDateTime end = value.getRangeEnd();

        // Если одна из дат не указана, тогда всё ок
        if (start == null || end == null) {
            return true;
        }

        return start.isBefore(end);
    }
}