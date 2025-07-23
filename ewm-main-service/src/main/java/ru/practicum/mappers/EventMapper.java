package ru.practicum.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;

@Mapper(componentModel = "spring")
public interface EventMapper {


    @Mapping(target = "category",ignore = true)
    @Mapping(source = "eventDate", target = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    Event mapToEvent(NewEventDto newEventDto);

    @Mapping(target = "category", source = "categoryDto")
    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "title", source = "event.title")
    EventFullDto mapToEventFullDto(Event event, CategoryDto categoryDto);

    EventShortDto toEventShortDto(Event event);

}
