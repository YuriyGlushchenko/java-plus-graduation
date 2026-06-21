package ru.practicum.ewm.event.dto.paramDto;


import java.time.LocalDateTime;


// обобщающий интерфейс для AdminUserEventParam и PublicUserEventParam, чтобы их обоих можно было проверять в кастомном валидаторе
public interface DateRangeValidatable {
    LocalDateTime getRangeStart();

    LocalDateTime getRangeEnd();
}
