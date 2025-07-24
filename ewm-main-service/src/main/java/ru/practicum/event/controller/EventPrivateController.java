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
        return eventService.createEvent(userId, eventDto);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventFullDto update(@PathVariable Long userId, @PathVariable Long eventId, @RequestBody @Valid UpdateEventUserRequest eventDto, HttpServletRequest request) {
        System.out.println(eventDto.toString());
        log.info("Запрос на обновление события с id:{},пользователем с id:{},{}", userId, eventId, eventDto.toString());
        return eventService.updateEventUser(userId, eventId, eventDto, request);
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public EventFullDto findEventById(@PathVariable Long eventId, @PathVariable Long userId) {
        log.info("Запрос на получение события с id:{}", eventId);
        return eventService.getEventById(eventId, userId);

    }

    @GetMapping("/users/{userId}/events")
    public Collection<EventShortDto> findAllByPrivate(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(required = false, defaultValue = "10") @Positive Integer size,
            HttpServletRequest request
    ) {
        log.info("Приватный запрос на получение событий userId:{},from:{},size:{} ", userId, from, size);
        return eventService.getAllEventsPrivate(userId, from, size, request);
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public Collection<ParticipationRequestDto> getByEventId(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Приватный запрос на получение события,userId:{},eventId:{}", userId, eventId);
        return eventService.getByEventId(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateStatus(@PathVariable Long userId, @PathVariable Long eventId, @RequestBody EventRequestStatusUpdateRequest requestsToUpdate) {
        log.info("Заапрос на обновление userId:{},eventId:{},{}", userId, eventId, requestsToUpdate);
        return eventService.updateStatus(userId, eventId, requestsToUpdate);

    }
}
