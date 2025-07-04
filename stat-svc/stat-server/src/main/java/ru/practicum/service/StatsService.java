package ru.practicum.service;


import ru.practicum.EndpointHitDto;
import ru.practicum.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;


public interface StatsService {

    EndpointHitDto addRequest(EndpointHitDto endpointHitDto);

    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris,Boolean unique);

}
