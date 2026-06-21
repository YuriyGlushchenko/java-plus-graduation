package ru.practicum.ewm.event.dto.paramDto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateRangeValidator.class)
@Documented
public @interface ValidDateRange {
    String message() default "rangeStart must be before rangeEnd";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}