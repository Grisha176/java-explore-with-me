package ru.practicum.stat.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.stat.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("GET запрос на получение статистики с start={}, end={}, uris={}, unique={}", start, end, uris, unique);

        return statsService.getStats(start, end, uris, unique);
    }

    @PostMapping("/hit")
    public EndpointHitDto create(@RequestBody EndpointHitDto endpointHitDto) {
        log.info("POST запрос на создание нового EndpointHit {}", endpointHitDto);
        return statsService.addRequest(endpointHitDto);
    }


}
