package ru.practicum.stat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.stat.dao.StatsRepository;
import ru.practicum.stat.mapper.StatsMapper;
import ru.practicum.stat.model.EndpointHit;
import ru.practicum.stat.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
        log.info("Успешное оздание EndpointHit с данными: {}", endpointHitDto.toString());

        return statsMapper.mapToEndpointHitDto(endpointHit);
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        log.info("Получение статистики с start={}, end={}, uris={}, unique={}", start, end, uris, unique);

        if (start != null && end != null && start.isAfter(end)) {
            log.warn("Некорректный запрос: start={} позже end={}", start, end);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date must be before end date");
        }

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
        log.info("Получена статистика: {}", viewStats);
        log.info("Имеющ стат" + statsRepository.findAll().stream().toList().toString());
        for (EndpointHit endpointHit : statsRepository.findAll()) {
            log.info("Имеющ стат" + endpointHit.toString());
        }

        return viewStats != null ? viewStats.stream()
                .map(statsMapper::mapToViewStatsDto)
                .collect(Collectors.toList()) : List.of();
    }


}
