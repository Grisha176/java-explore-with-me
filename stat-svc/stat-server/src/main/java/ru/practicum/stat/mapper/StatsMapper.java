package ru.practicum.stat.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.stat.model.EndpointHit;
import ru.practicum.stat.model.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface StatsMapper {

    ViewStatsDto mapToViewStatsDto(ViewStats viewStats);


    @Mapping(target = "timestamp", source = "timestamp", qualifiedByName = "stringToLocalDateTime")
    EndpointHit mapToEndpointHit(EndpointHitDto endpointHitDto);

    @Mapping(target = "timestamp", source = "timestamp", qualifiedByName = "localDateTimeToString")
    EndpointHitDto mapToEndpointHitDto(EndpointHit endpointHit);

    @Named("stringToLocalDateTime")
    default LocalDateTime stringToLocalDateTime(String date) {
        if (date == null) return null;

        // Укажи явный формат даты + времени
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return LocalDateTime.parse(date, formatter);
    }

    @Named("localDateTimeToString")
    default String localDateTimeToString(LocalDateTime date) {
        if (date == null) return null;
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }


}
