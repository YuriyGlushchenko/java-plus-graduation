package ru.practicum.ewm.compilation.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.compilation.dto.CompilationMapper;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.exceptions.exceptions.NotFoundException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventService eventService;

    @Override
    @Transactional
    public CompilationDto postCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto);
        Compilation saved = compilationRepository.postCompilation(compilation);

        List<Long> eventIds = saved.getEvents().stream()
                .map(Event::getId)
                .toList();

        List<EventShortDto> eventDtos = getEventShortDtos(eventIds);

        return CompilationMapper.toCompilationDto(saved, eventDtos);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Подборка не найдена");
        }
        compilationRepository.deleteCompilation(compId);
    }

    @Override
    @Transactional
    public CompilationDto patchCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        Compilation compilation = compilationRepository.getCompilationById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id=" + compId + " не найдена"));

        CompilationMapper.updateCompilationFromRequest(updateRequest, compilation);

        Compilation updated = compilationRepository.patchCompilation(compId, compilation);

        List<Long> eventIds = updated.getEvents().stream()
                .map(Event::getId)
                .toList();
        List<EventShortDto> eventDtos = getEventShortDtos(eventIds);

        return CompilationMapper.toCompilationDto(updated, eventDtos);
    }


    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        List<Compilation> compilations = compilationRepository.getCompilations(pinned, from, size);

        Set<Long> allEventIds = compilations.stream()
                .flatMap(c -> c.getEvents().stream().map(Event::getId))
                .collect(Collectors.toSet());

        Map<Long, EventShortDto> eventsMap = eventService.getShortDtosByIds(allEventIds)
                .stream()
                .collect(Collectors.toMap(EventShortDto::getId, e -> e));

        return compilations.stream()
                .map(c -> {
                    List<EventShortDto> compEvents = c.getEvents().stream()
                            .map(Event::getId)
                            .map(eventsMap::get)
                            .filter(Objects::nonNull)
                            .toList();
                    return CompilationMapper.toCompilationDto(c, compEvents);
                })
                .toList();
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.getCompilationById(compId)
                .orElseThrow();

        List<Long> eventIds = compilation.getEvents().stream()
                .map(Event::getId)
                .toList();

        List<EventShortDto> events = eventService.getShortDtosByIds(eventIds);

        return CompilationMapper.toCompilationDto(compilation, events);
    }

    private List<EventShortDto> getEventShortDtos(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyList();
        }
        return eventService.getShortDtosByIds(eventIds);
    }
}
