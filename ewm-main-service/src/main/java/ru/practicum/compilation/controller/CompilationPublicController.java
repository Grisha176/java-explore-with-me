package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.service.CompilationService;

import java.util.List;

@RestController("/compilations")
@RequiredArgsConstructor
@Slf4j
public class CompilationPublicController {

    private final CompilationService compilationService;


    @GetMapping
    public List<CompilationDto> getAllCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("Запрос на получение подборки всех событий");
        return compilationService.getAllCompilationPublic(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto findCompilationById(@PathVariable Long compId) {
        log.info("Запрос на получение подборки событий по id = {}", compId);
        return compilationService.getById(compId);
    }
}
