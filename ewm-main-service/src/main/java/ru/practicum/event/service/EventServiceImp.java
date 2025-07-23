package ru.practicum.event.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.dto.*;
import ru.practicum.mappers.*;
import ru.practicum.client.StatisticsClient;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.category.dao.CategoryRepository;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.event.dao.EventRepository;
import ru.practicum.event.enums.EventState;
import ru.practicum.event.enums.EventStateAction;
import ru.practicum.event.enums.SortType;
import ru.practicum.event.model.Event;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ValidateDataException;
import ru.practicum.location.Location;
import ru.practicum.location.dao.LocationRepository;
import ru.practicum.request.dao.EventRequestRepository;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.enums.RequestStatus;
import ru.practicum.request.model.EventRequest;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dao.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.event.enums.EventStateAction.PUBLISH_EVENT;
import static ru.practicum.event.enums.EventStateAction.REJECT_EVENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImp implements EventService {


    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationMapper locationMapper;
    private final LocationRepository locationRepository;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final StatisticsClient statClient;
    private final EventRequestRepository eventRequestRepository;
    private final EventRequestMapper eventRequestMapper;

    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        log.info("Попытка создать событие пользователем {},{}", userId, newEventDto);
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id:" + userId + " не найден"));
        Category category = categoryRepository.findById(newEventDto.getCategory().intValue())
                .orElseThrow(() -> new NotFoundException("Категория с ID " + newEventDto.getCategory() + " не найдена"));
        Event event = eventMapper.mapToEvent(newEventDto);
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidateDataException("Время начала события должно быть хотя бы на 2 часа позже");
        }

        Location location = locationMapper.mapToLocation(newEventDto.getLocation());
        location = locationRepository.save(location);

        event.setState(EventState.PENDING);
        event.setLocation(location);
        event.setInitiator(user);
        event.setCreatedOn(LocalDateTime.now());
        event.setCategory(category);

        if (newEventDto.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }

        event = eventRepository.save(event);
        log.info("Успешное создание события {}", event.toString());
        CategoryDto categoryDto = categoryMapper.mapToCategoryDto(event.getCategory());
        return eventMapper.mapToEventFullDto(event, categoryDto);
    }

    @Override
    public List<EventShortDto> getAllEventsPrivate(Long userId, int from, int size) {
        log.info("Получение всех событий пользователя с id {}", userId);
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id:" + userId + " не найден"));
        Pageable pageable = PageRequest.of(from, size);
        List<Event> events = eventRepository.findAll(pageable).getContent();
        return events.stream()
                .map(event -> {
                    CategoryDto categoryDto = categoryMapper.mapToCategoryDto(event.getCategory());
                    UserShortDto userShortDto = userMapper.toUserShortDto(event.getInitiator());
                    long views = getViews(event.getId(), event.getCreatedOn());

                    EventShortDto eventShortDto = eventMapper.toEventShortDto(event);
                    eventShortDto.setCategory(categoryDto);
                    eventShortDto.setInitiator(userShortDto);
                    eventShortDto.setViews((int) views);

                    return eventShortDto;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getEventById(Long eventId, Long userId) {
        log.info("Получение события {}, пользователя с id {}", eventId, userId);
        log.info("Получение события от пользователя {}", userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id:" + userId + " не найден"));


        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id:" + eventId + " не найдено"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Пользователь не является инициатором события");
        }
        CategoryDto categoryDto = categoryMapper.mapToCategoryDto(event.getCategory());
        return eventMapper.mapToEventFullDto(event, categoryDto);
    }

    @Transactional
    @Override
    public EventFullDto updateEventUser(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        log.info("Попытка обновить событие: {},{}", eventId, updateEventUserRequest);
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id:" + userId + " не найден"));
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(() -> new NotFoundException("Событие с id:" + eventId + " не найдено"));
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Опубликованные события нельзя изменить");
        }
        event = updateEventFromDto(updateEventUserRequest, event);
        event = eventRepository.save(event);
        event.setViews(getViews(event.getId(), event.getCreatedOn()));
        log.info("Успешное обновление события");
        CategoryDto categoryDto = categoryMapper.mapToCategoryDto(event.getCategory());
        return eventMapper.mapToEventFullDto(event, categoryDto);
    }

    @Transactional
    @Override
    public List<EventFullDto> getAllEventsAdmin(List<Long> users, List<String> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size, HttpServletRequest request) {

        Pageable pageable = PageRequest.of(from, size);


        List<EventState> eventStates;
        if (states != null) {
            eventStates = states.stream().map(EventState::valueOf).toList();
        } else {
            eventStates = null;
        }


        List<Event> eventList;
        try {
            //eventList = eventRepository.findAll(users, eventStates, categories,rangeStart,rangeEnd, pageable);
            eventList = eventRepository.findAll(users, eventStates, categories, rangeStart, rangeEnd, pageable);
            // Вот так бери контент
            log.info("eventsList:" + eventList);
        } catch (Exception e) {
            log.error("Ошибка при выполнении запроса к БД: ", e);
            throw new RuntimeException("Ошибка при получении данных из базы данных", e);
        }


        List<Long> eventIds = eventList.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsForEvents(eventIds);

        return eventList.stream()
                .map(event -> {
                    CategoryDto categoryDto = categoryMapper.mapToCategoryDto(event.getCategory());
                    EventFullDto dto = eventMapper.mapToEventFullDto(event, categoryDto);
                    dto.setViews(getViews(event.getId(), event.getCreatedOn()));
                    dto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L).intValue());
                    return dto;
                })
                .collect(Collectors.toList());

    }

    private Map<Long, Long> getConfirmedRequestsForEvents(List<Long> eventIds) {
        List<Object[]> results = eventRequestRepository.countByEventIdInAndStatus(eventIds, RequestStatus.CONFIRMED);
        Map<Long, Long> confirmedRequestsMap = new HashMap<>();
        for (Object[] result : results) {
            Long eventId = (Long) result[0];
            Long count = (Long) result[1];
            confirmedRequestsMap.put(eventId, count);
        }
        return confirmedRequestsMap;
    }

    @Override
    public List<ParticipationRequestDto> getByEventId(Long eventInitiatorId, Long eventId) {

        userRepository.findById(eventInitiatorId)
                .orElseThrow(() -> new NotFoundException("Пользователь c ID " + eventInitiatorId + " не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id:" + eventId + " не найдено"));

        if (!event.getInitiator().getId().equals(eventInitiatorId)) {
            throw new IllegalArgumentException("Пользователь не является инициатором события.");
        }

        List<EventRequest> requests = eventRequestRepository.findAllByEventId(eventId);

        return requests.stream()
                .map(eventRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult updateStatus(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest dto) {
        log.info("Изменение статуса заявок: userId = {}, eventId = {}, status = {}", userId, eventId, dto.getStatus());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь c ID " + userId + " не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие c ID " + eventId + " не найдено"));

        List<EventRequest> requests = eventRequestRepository.findAllById(dto.getRequestIds());
        EventRequestStatusUpdateResult result = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(new ArrayList<>())
                .rejectedRequests(new ArrayList<>())
                .build();

        if (requests.isEmpty()) {
            return result;
        }

        RequestStatus targetStatus = RequestStatus.valueOf(String.valueOf(dto.getStatus()));

        int limit = event.getParticipantLimit();
        long confirmed = eventRequestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);

        if (targetStatus == RequestStatus.CONFIRMED) {
            if (limit != 0 && confirmed >= limit) {
                throw new ConflictException("Достигнут лимит участников.");
            }

            for (EventRequest request : requests) {
                if (!request.getEvent().getId().equals(eventId)) {
                    throw new NotFoundException("Запрос не относится к данному событию.");
                }
                if (request.getStatus() != RequestStatus.PENDING) {
                    throw new ConflictException("Изменять можно только заявки в статусе PENDING.");
                }
                request.setStatus(RequestStatus.CONFIRMED);
                result.getConfirmedRequests().add(eventRequestMapper.toParticipationRequestDto(request));
                confirmed++;
                if (confirmed == limit) {
                    break;
                }
            }

        } else if (targetStatus == RequestStatus.REJECTED) {
            for (EventRequest request : requests) {
                if (!request.getEvent().getId().equals(eventId)) {
                    throw new NotFoundException("Запрос не относится к данному событию.");
                }
                if (request.getStatus() != RequestStatus.PENDING) {
                    throw new ConflictException("Изменять можно только заявки в статусе PENDING.");
                }
                request.setStatus(RequestStatus.REJECTED);
                result.getRejectedRequests().add(eventRequestMapper.toParticipationRequestDto(request));
            }
        }

        eventRequestRepository.saveAll(requests);
        return result;
    }


    private void rejectRequests(EventRequestStatusUpdateResult result, List<EventRequest> requests) {
        for (EventRequest eventRequest : requests) {

            if (eventRequest.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Можно отклонить запрс только в статусе ожидания");
            }
            eventRequest.setStatus(RequestStatus.REJECTED);
            EventRequest updateRequest = eventRequestRepository.save(eventRequest);
            result.getRejectedRequests().add(eventRequestMapper.toParticipationRequestDto(updateRequest));
        }
    }

    @Transactional
    @Override
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с id:" + eventId + " не найдено"));
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Опубликованные события нельзя изменить");
        }
        LocalDateTime eventDate = updateEventAdminRequest.getEventDate() != null ? updateEventAdminRequest.getEventDate() : event.getEventDate();
        if (eventDate.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ValidateDataException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
        }
        validateUpdateAdmin(event.getState(), updateEventAdminRequest.getStateAction());
        event = updateEventFromAdminDto(updateEventAdminRequest, event);
        event = eventRepository.save(event);
        event.setViews(getViews(event.getId(), event.getCreatedOn()));
        CategoryDto categoryDto = categoryMapper.mapToCategoryDto(event.getCategory());
        return eventMapper.mapToEventFullDto(event, categoryDto);

    }


    @Override
    public List<EventShortDto> getAllEventPublicRequest(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, SortType sortType, int from, int size, HttpServletRequest httpServletRequest) {

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new IllegalArgumentException("Время начала должно быть раньше конца");
        }


        Pageable pageable = PageRequest.of(from, size);
        Specification<Event> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("state"), EventState.PUBLISHED));


            if (text != null) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), "%" + text.toLowerCase() + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + text.toLowerCase() + "%")
                ));
            }

            if (categories != null && categories.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categories));
            }

            if (paid != null) {
                predicates.add(criteriaBuilder.equal(root.get("paid"), paid));
            }

            if (rangeStart == null && rangeEnd == null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), LocalDateTime.now()));
            } else {
                predicates.add(criteriaBuilder.between(root.get("eventDate"), rangeStart, rangeEnd));
            }

            if ((onlyAvailable)) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.equal(root.get("participantLimit"), 0),
                        criteriaBuilder.greaterThan(root.get("participantLimit"), root.get("confirmedRequests"))
                ));
            }


            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };


        Page<Event> eventPage = eventRepository.findAll(spec, pageable);

        statClient.create(httpServletRequest);

        List<EventShortDto> eventShortDtos = eventPage.getContent().stream()
                .map(event -> {
                    EventShortDto eventDto = eventMapper.toEventShortDto(event);
                    eventDto.setViews(getViews(event.getId(), event.getCreatedOn()));
                    eventDto.setConfirmedRequests(eventDto.getConfirmedRequests());
                    return eventDto;
                })
                .collect(Collectors.toList());

        if (sortType.equals(SortType.EVENT_DATE)) {
            eventShortDtos.sort(Comparator.comparing(EventShortDto::getEventDate));
        } else if (sortType.equals(SortType.VIEWS)) {
            eventShortDtos.sort(Comparator.comparing(EventShortDto::getViews));
        }

        return eventShortDtos;
    }

    @Override
    public EventFullDto getEventByIdPublic(Long eventId, HttpServletRequest httpServletRequest) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с id:" + eventId + " не найдено"));
        statClient.create(httpServletRequest);

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Событие не опубликовано");
        }

        CategoryDto categoryDto = categoryMapper.mapToCategoryDto(event.getCategory());
        EventFullDto eventShortDto = eventMapper.mapToEventFullDto(event, categoryDto);
        eventShortDto.setViews(getViews(event.getId(), event.getCreatedOn(), httpServletRequest).intValue());
        return eventShortDto;


    }

    private Long getViews(Long eventId, LocalDateTime createdOn, HttpServletRequest request) {
        LocalDateTime end = LocalDateTime.now();
        String uri = request.getRequestURI();
        Boolean unique = true;
        Long defaultViews = 0L;
        ObjectMapper mapper = new ObjectMapper();

        try {
            ResponseEntity<Object> statsResponse = statClient.getStats(createdOn, end, List.of(uri), unique);
            log.info("Запрос к statClient: URI={}, from={}, to={}, unique={}", uri, createdOn, end, unique);
            log.info("Ответ от statClient: status={}, body={}", statsResponse.getStatusCode(), statsResponse.getBody());
            if (statsResponse.getStatusCode().is2xxSuccessful() && statsResponse.hasBody()) {
                Object body = statsResponse.getBody();
                if (body != null) {
                    try {
                        ViewStatsDto[] statsArray = mapper.convertValue(body, ViewStatsDto[].class);
                        List<ViewStatsDto> stats = Arrays.asList(statsArray);

                        if (!stats.isEmpty()) {
                            return stats.getLast().getHits();
                        } else {
                            log.info("Нет данных статистики для события {}", eventId);
                        }
                    } catch (Exception e) {
                        log.error("Ошибка преобразования данных статистики для события {}: {}", eventId, e.getMessage());
                        return defaultViews;
                    }
                } else {
                    log.warn("Тело ответа от statClient пустое для события {}", eventId);
                }
            } else {
                log.warn("Неуспешный ответ от statClient для события {}: {}", eventId, statsResponse.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Ошибка при получении статистики для события {}: {}", eventId, e.getMessage());
        }
        return defaultViews;
    }

    private Integer getViews(Long eventId, LocalDateTime createdOn) {
        LocalDateTime start = createdOn;
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = List.of("/events/" + eventId);
        Boolean unique = true;

        ResponseEntity<Object> statsResponse = statClient.getStats(start, end, uris, unique);

        if (statsResponse.getStatusCode().is2xxSuccessful() && statsResponse.getBody() != null) {
            List<Map<String, Object>> stats = (List<Map<String, Object>>) statsResponse.getBody();
            if (!stats.isEmpty()) {
                Map<String, Object> stat = stats.getFirst();
                Long hits = (Long) stat.get("hits");
                return Math.toIntExact(hits);
            }
        } else {
            log.warn("Ошибка получение статисктики.Код:" + statsResponse.getStatusCode());
        }
        return 0;
    }

    private Map<Long, Long> getViewsAllEvents(List<Event> events) {
        LocalDateTime start = LocalDateTime.now().minusYears(1);
        LocalDateTime end = LocalDateTime.now().plusYears(1);
        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());
        ResponseEntity<Object> response = statClient.getStats(start, end, uris, true);


        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            List<ViewStatsDto> viewStatsList = Arrays.asList(new ObjectMapper().convertValue(response.getBody(), ViewStatsDto[].class));
            return viewStatsList.stream()
                    .collect(Collectors.toMap(
                            viewStats -> Long.parseLong(viewStats.getUri().substring("/events/".length())),
                            ViewStatsDto::getHits
                    ));
        } else {
            log.warn("Не удалось получить статистику просмотров. Код ответа: {}", response.getStatusCodeValue());
            return Collections.emptyMap();
        }
    }

    private Event updateEventFromDto(UpdateEventUserRequest dto, Event event) {
        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getEventDate() != null) {
            if (dto.getEventDate().isBefore(LocalDateTime.now())) {
                throw new ValidateDataException("Событие не может быть в прошлом");
            }
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getLocation() != null) event.setLocation(dto.getLocation());
        if (dto.getPaid() != null) event.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());
        if (dto.getTitle() != null) event.setTitle(dto.getTitle());

        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(Integer.parseInt(dto.getCategory().toString()))
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            event.setCategory(category);
        }
        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
            }
        }
        return event;

    }

    private void validateRequestWithEvent(List<EventRequest> requests, Long eventId) {
        for (EventRequest request : requests) {
            if (!request.getRequester().getId().equals(eventId)) {
                throw new ConflictException("Запрос с id:" + request.getId() + " не относится к событию с id:" + eventId);
            }
        }


    }

    private static Event updateEventFromAdminDto(UpdateEventAdminRequest dto, Event event) {
        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (dto.getCategory() != null) {
            Category category = new Category();
            category.setId(dto.getCategory());
            event.setCategory(category);
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getEventDate() != null) {
            event.setEventDate(LocalDateTime.parse(dto.getEventDate().toString()));
        }
        if (dto.getLocation() != null) {
            event.setLocation(dto.getLocation());
        }
        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        }
        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }
        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case PUBLISH_EVENT:
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }

        return event;
    }

    private void validateUpdateAdmin(EventState eventState, EventStateAction eventStateAction) {

        if (eventStateAction != null) {
            if (eventStateAction.equals(PUBLISH_EVENT) && !eventState.equals(EventState.PENDING)) {
                throw new ConflictException("событие можно публиковать, только если оно в состоянии ожидания публикации");
            }

            if (eventStateAction.equals(REJECT_EVENT) && eventState.equals(EventState.PUBLISHED)) {
                throw new ConflictException("событие можно отклонить, только если оно еще не опубликовано");
            }

        }


    }
}
