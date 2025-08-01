package ru.practicum.request.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.EventRequestService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class RequestController {

    private final EventRequestService eventRequestService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/users/{userId}/requests")
    public ParticipationRequestDto create(@PathVariable(value = "userId") @Min(0) Long userId,
                                          @RequestParam(name = "eventId") @Min(0) Long eventId) {
        log.info("POST запрос на создание запроса на участие в событии с id= {}  пользователя с id= {}",
                eventId, userId);
        ParticipationRequestDto createdRequest = eventRequestService.create(userId, eventId);
        log.info("Создан запрос на участие: {}", createdRequest);
        return createdRequest;
    }

    @GetMapping("/users/{userId}/requests")
    public List<ParticipationRequestDto> getAllRequests(@PathVariable(value = "userId") @Min(0) Long userId) {
        log.info("GET запрос на получение всех запросов на участие в событиях пользователя с id= {}", userId);
        return eventRequestService.getRequestsByUserId(userId);
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto canceledRequest(@PathVariable(value = "userId") @Min(0) Long userId,
                                                   @PathVariable(value = "requestId") @Min(0) Long requestId) {
        log.info("PATCH запрос на отмену запроса пользователем с id= {}", userId);
        return eventRequestService.cancelRequest(userId, requestId);
    }
}
