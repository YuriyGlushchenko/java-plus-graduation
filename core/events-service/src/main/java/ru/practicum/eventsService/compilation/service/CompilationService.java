package ru.practicum.eventsService.compilation.service;



import ru.practicum.eventsService.compilation.dto.CompilationDto;
import ru.practicum.eventsService.compilation.dto.NewCompilationDto;
import ru.practicum.eventsService.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto postCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilation(Long compId);

    CompilationDto patchCompilation(Long compId, UpdateCompilationRequest updateRequest);

    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);

    CompilationDto getCompilationById(Long compId);
}
