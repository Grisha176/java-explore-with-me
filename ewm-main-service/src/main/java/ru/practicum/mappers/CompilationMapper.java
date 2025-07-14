package ru.practicum.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.repository.query.Param;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationRequestDto;
import ru.practicum.compilation.dto.UpdateCompilationRequestDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.dto.EventShortDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompilationMapper {

    @Mapping(target = "events",ignore = true)
    Compilation mapToCompilation(NewCompilationRequestDto newCompilationRequestDto);

    @Mapping(source = "events",target = "events")
    CompilationDto mapToCompilationDto(Compilation compilation,@Param("events") List<EventShortDto> events);


}
