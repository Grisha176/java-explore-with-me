package ru.practicum.compilation.service;

import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationRequestDto;
import ru.practicum.compilation.dto.UpdateCompilationRequestDto;

import java.util.List;

public interface CompilationService {


    CompilationDto create(NewCompilationRequestDto newCompilationRequestDto);

    void deleteComp(Long compId);

    CompilationDto updateComp(Long compId, UpdateCompilationRequestDto updateCompilationRequestDto);

    List<CompilationDto> getAllCompilationPublic(Boolean pinned,int from,int size);

    CompilationDto getById(Long compId);


}
