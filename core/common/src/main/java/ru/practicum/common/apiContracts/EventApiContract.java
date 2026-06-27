package ru.practicum.common.apiContracts;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.common.dto.events.EventBaseDto;
import ru.practicum.common.dto.events.EventFullDto;

public interface EventApiContract {
    @GetMapping("/{id}")
    EventBaseDto getBaseEventInfo(@PathVariable long id);
}
