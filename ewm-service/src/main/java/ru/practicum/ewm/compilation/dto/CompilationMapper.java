package ru.practicum.ewm.compilation.dto;

import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.model.Event;

import java.util.ArrayList;
import java.util.List;

public class CompilationMapper {

    public static Compilation toCompilation(NewCompilationDto dto) {
        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned() != null && dto.getPinned())
                .events(dto.getEvents() != null ?
                        dto.getEvents().stream()
                                .map(id -> Event.builder().id(id).build())
                                .toList() : new ArrayList<>())
                .build();
    }

    public static void updateCompilationFromRequest(UpdateCompilationRequest request, Compilation compilation) {
        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }

        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }

        if (request.getEvents() != null) {
        // т.к. связь многие-ко-многим, для создания записи в промеж. таблице достаточно просто id, без остальных полей.
            List<Event> newEvents = request.getEvents().stream()
                    .map(id -> Event.builder().id(id).build())
                    .toList();

            compilation.setEvents(newEvents);
        }
    }

    public static CompilationDto toCompilationDto(Compilation compilation, List<EventShortDto> events) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(events)
                .build();
    }
}