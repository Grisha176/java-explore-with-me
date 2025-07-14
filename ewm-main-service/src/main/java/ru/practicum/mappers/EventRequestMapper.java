package ru.practicum.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.EventRequest;

@Mapper(componentModel = "spring")
public interface EventRequestMapper {

    @Mapping(source = "eventId", target = "event")
    @Mapping(source = "requesterId", target = "requester")
    ParticipationRequestDto toParticipationRequestDto(EventRequest eventRequest);


}
