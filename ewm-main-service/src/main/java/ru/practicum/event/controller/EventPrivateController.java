package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.service.EventService;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.Collection;


@RestController
@Slf4j
@RequiredArgsConstructor
public class EventPrivateController {


    private final EventService eventService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/users/{userId}/events")
    public EventFullDto create(@PathVariable Long userId, @RequestBody @Valid NewEventDto eventDto) {
        log.info("Запрос на создание события userId:{},{}", userId, eventDto);
        final EventFullDto event = eventService.createEvent(userId, eventDto);
        return event;
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventFullDto update(@PathVariable Long userId, @PathVariable Long eventId, @RequestBody @Valid UpdateEventUserRequest eventDto) {
        System.out.println(eventDto.toString());
        log.info("Запрос на обновление события с id:{},пользователем с id:{},{}", userId, eventId, eventDto.toString());
        return eventService.updateEventUser(userId, eventId, eventDto);
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public EventFullDto findEventById(@PathVariable Long eventId, HttpServletRequest request) {
        log.info("Запрос на получение события с id:{}", eventId);
        final EventFullDto event = eventService.getEventByIdPublic(eventId, request);
        return event;
    }

    @GetMapping("/users/{userId}/events")
    public Collection<EventShortDto> findAllByPrivate(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(required = false, defaultValue = "10") @Positive Integer size
    ) {
        log.info("Приватный запрос на получение событий userId:{},from:{},size:{} ", userId, from, size);
        final Collection<EventShortDto> events = eventService.getAllEventsPrivate(userId, from, size);
        return events;
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public Collection<ParticipationRequestDto> getByEventId(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Приватный запрос на получение события,userId:{},eventId:{}", userId, eventId);
        final Collection<ParticipationRequestDto> requests = eventService.getByEventId(userId, eventId);
        return requests;
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateStatus(@PathVariable Long userId, @PathVariable Long eventId, @RequestBody EventRequestStatusUpdateRequest requestsToUpdate) {
        log.info("Заапрос на обновление userId:{},eventId:{},{}", userId, eventId, requestsToUpdate);
        final EventRequestStatusUpdateResult result = eventService.updateStatus(userId, eventId, requestsToUpdate);
        return result;
    }
}
