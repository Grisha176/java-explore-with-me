package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.event.dto.*;
import ru.practicum.event.enums.SortType;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

     EventFullDto createEvent(Long userId,NewEventDto newEventDto);

     List<EventShortDto> getAllEventsPrivate(Long userId, int from, int size);

     List<EventFullDto> getAllEventsAdmin(List<Long> users, List<String> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd,int from,int size);

     List<EventShortDto> getAllEventPublicRequest(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, SortType sortType,int from,int size,HttpServletRequest httpServletRequest);

     EventFullDto getEventByIdPublic(Long eventId,HttpServletRequest httpServletRequest);

     EventFullDto getEventById(Long eventId, Long userId, HttpServletRequest httpServletRequest);

     EventFullDto updateEventUser(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

     EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

     List<ParticipationRequestDto> getByEventId(Long eventInitiatorId, Long eventId);

     EventRequestStatusUpdateResult updateStatus(Long eventInitiatorId, Long eventId, EventRequestStatusUpdateRequest updateStatusRequest);


}
