package ru.practicum.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;

@Mapper(componentModel = "spring")
public interface EventMapper {


    @Mapping(target = "category",ignore = true)
    Event mapToEvent(NewEventDto newEventDto);

    @Mapping(target = "category",ignore = true)
    EventFullDto mapToEventFullDto(Event event);

    EventShortDto toEventShortDto(Event event);

}
