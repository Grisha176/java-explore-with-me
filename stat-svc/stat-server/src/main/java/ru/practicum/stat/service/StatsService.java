package ru.practicum.stat.service;


import ru.dto.EndpointHitDto;
import ru.practicum.stat.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;


public interface StatsService {

    EndpointHitDto addRequest(EndpointHitDto endpointHitDto);

    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris,Boolean unique);

}
