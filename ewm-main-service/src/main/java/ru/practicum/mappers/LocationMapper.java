package ru.practicum.mappers;

import org.mapstruct.Mapper;
import ru.practicum.location.Location;
import ru.practicum.location.LocationDto;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    Location mapToLocation(LocationDto locationDto);
}
