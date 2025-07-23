package ru.practicum.compilation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationRequestDto;
import ru.practicum.compilation.dto.UpdateCompilationRequestDto;
import ru.practicum.compilation.service.CompilationService;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CompilationAdminController {

    private final CompilationService compilationService;


    @PostMapping("/admin/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@RequestBody @Valid NewCompilationRequestDto newCompilationRequestDto) {
        log.info("Создание нового события {}", newCompilationRequestDto);
        return compilationService.create(newCompilationRequestDto);
    }

    @PatchMapping("/admin/compilations/{compId}")
    public CompilationDto update(@Valid @RequestBody UpdateCompilationRequestDto updateCompilation,
                                 @PathVariable Long compId) {
        log.info("Запрос на обновление подборки событий -ADMIN");
        return compilationService.updateComp(compId, updateCompilation);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/admin/compilations/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Запрос на удаление подборки событий - ADMIN");
        compilationService.deleteComp(id);
    }

}
