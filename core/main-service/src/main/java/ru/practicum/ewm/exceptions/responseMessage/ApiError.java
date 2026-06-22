package ru.practicum.ewm.exceptions.responseMessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private List<String> errors;      // стектрейсы или описания
    private String message;            // сообщение об ошибке
    private String reason;             // общее описание причины
    private String status;             // код статуса HTTP
    private String timestamp;          // "yyyy-MM-dd HH:mm:ss"
}