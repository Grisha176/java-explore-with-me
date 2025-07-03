package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.EndpointHitDto;
import ru.practicum.dao.StatsRepository;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final StatsMapper statsMapper;

    @Override
    public EndpointHitDto addRequest(EndpointHitDto endpointHitDto) {
        log.info("Создание EndpointHit с данными: {}", endpointHitDto);

        EndpointHit endpointHit = statsMapper.mapToEndpointHit(endpointHitDto);
        endpointHit = statsRepository.save(endpointHit);
        return statsMapper.mapToEndpointHitDto(endpointHit);
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        log.info("Получение статистики с start={}, end={}, uris={}, unique={}", start, end, uris, unique);

        List<ViewStats> viewStats;
        if (unique) {
            if (uris != null && !uris.isEmpty()) {
                viewStats = statsRepository.findStatsUniqueIp(start, end, uris);
            } else {
                viewStats = statsRepository.findStatsUniqueIpAllUris(start, end);
            }
        } else {
            if (uris != null && !uris.isEmpty()) {
                viewStats = statsRepository.findStats(start, end, uris);
            } else {
                viewStats = statsRepository.findStatsAllUris(start, end);
            }
        }
        return viewStats;
    }




}
