package ru.practicum.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface EventMapper {


    default String formatLocalDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
    }

    @Mapping(target = "category",ignore = true)
    @Mapping(source = "eventDate", target = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    Event mapToEvent(NewEventDto newEventDto);

    @Mapping(target = "category", source = "categoryDto")
    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "title", source = "event.title")
    @Mapping(target = "eventDate", source = "event.eventDate")
    EventFullDto mapToEventFullDto(Event event, CategoryDto categoryDto);

    EventShortDto toEventShortDto(Event event);

}
