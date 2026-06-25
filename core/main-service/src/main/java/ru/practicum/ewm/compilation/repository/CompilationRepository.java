package ru.practicum.ewm.compilation.repository;

import ru.practicum.ewm.compilation.model.Compilation;

import java.util.List;
import java.util.Optional;

public interface CompilationRepository {
    Compilation postCompilation(Compilation compilation);

    void deleteCompilation(Long comId);

    Compilation patchCompilation(Long comId, Compilation compilation);

    List<Compilation> getCompilations(Boolean pinned, int from, int size);

    Optional<Compilation> getCompilationById(Long compId);

    boolean existsById(Long compId);

}
