package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.dao.EventRepository;
import ru.practicum.event.enums.EventState;
import ru.practicum.event.model.Event;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.DuplicatedException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mappers.EventRequestMapper;
import ru.practicum.request.dao.EventRequestRepository;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.enums.RequestStatus;
import ru.practicum.request.model.EventRequest;
import ru.practicum.user.dao.UserRepository;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventRequestServiceImpl implements EventRequestService {

    UserRepository userRepository;
    EventRepository eventRepository;
    EventRequestRepository eventRequestRepository;
    EventRequestMapper eventRequestMapper;

    @Override
    public ParticipationRequestDto create(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь c ID " + userId + " не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id= " + eventId + " не найдено"));

        validateNewRequest(event, userId, eventId);

        EventRequest request = new EventRequest();
        request.setCreated(LocalDateTime.now());
        request.setRequesterId(userId);
        request.setEventId(eventId);

        if (event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else if (event.getRequestModeration()) {
            request.setStatus(RequestStatus.PENDING);
        } else {
            request.setStatus(RequestStatus.CONFIRMED);
        }

        EventRequest savedRequest = eventRequestRepository.save(request);

        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <= eventRequestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)) {
            throw new ConflictException("Превышен лимит участников события");
        }

        return eventRequestMapper.toParticipationRequestDto(savedRequest);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> getRequestsByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь c ID " + userId + " не найден"));

        List<EventRequest> result = eventRequestRepository.findAllByRequesterId(userId);
        return result.stream().map(eventRequestMapper::toParticipationRequestDto).collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        EventRequest request = eventRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id:" + requestId + " не найден"));

        if (!request.getRequesterId().equals(userId)) {
            throw new ConflictException("Запрос не принадлежит пользователю");
        }

        if (request.getStatus() == RequestStatus.CANCELED || request.getStatus() == RequestStatus.REJECTED) {
            throw new ConflictException("Запрос уже отменен или отклонен");
        }

        request.setStatus(RequestStatus.CANCELED);
        EventRequest savedRequest = eventRequestRepository.save(request);
        return eventRequestMapper.toParticipationRequestDto(savedRequest);
    }

    private void validateNewRequest(Event event, Long userId, Long eventId) {
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Пользователь с id= " + userId + " не инициатор события");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Событие не опубликовано");
        }

        if (eventRequestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new DuplicatedException("Попытка добаления дубликата");
        }
    }
}
