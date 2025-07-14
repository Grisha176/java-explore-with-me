package ru.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Slf4j
public class EventAdminController {

    private final EventService eventService;

    @GetMapping
    public Collection<EventFullDto> get(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(required = false, defaultValue = "0")@PositiveOrZero Integer from,
            @RequestParam(required = false, defaultValue = "10")@Positive Integer size
    ) {
        log.info("Пришел GET запрос /admin/events с параметрами: users={}, states={}, categories={}, rangeStart={}, rangeEnd={}, from={}, size={}",
                users, states, categories, rangeStart, rangeEnd, from, size);
        final Collection<EventFullDto> events = eventService.getAllEventsAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
        log.info("Отправлен ответ GET /admin/events с телом: {}", events);
        return events;
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable Long eventId, @RequestBody @Valid UpdateEventAdminRequest eventDto) {
        log.info("Пришел PATCH запрос /admin/events/{} с телом {}", eventId, eventDto);
        final EventFullDto event = eventService.updateEventAdmin(eventId, eventDto);
        log.info("Отправлен ответ PATCH /admin/events/{} с телом: {}", eventId, event);
        return event;
    }
}
