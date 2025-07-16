package ru.practicum.event.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatisticsClient;
import ru.practicum.category.dao.CategoryRepository;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.event.dao.EventRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.enums.EventState;
import ru.practicum.event.enums.EventStateAction;
import ru.practicum.event.enums.SortType;
import ru.practicum.event.model.Event;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ValidateDataException;
import ru.practicum.location.Location;
import ru.practicum.location.dao.LocationRepository;
import ru.practicum.mappers.*;
import ru.practicum.request.dao.EventRequestRepository;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.enums.RequestStatus;
import ru.practicum.request.model.EventRequest;
import ru.practicum.stat.model.ViewStats;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dao.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {


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
    public EventFullDto createEvent(Long userId,NewEventDto newEventDto) {
        log.info("Попытка создать событие пользователем {},{}", userId, newEventDto);
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id:"+userId+" не найден"));
        Category category = categoryRepository.findById(newEventDto.getCategory().intValue())
                .orElseThrow(() -> new NotFoundException("Категория с ID " + newEventDto.getCategory() + " не найдена"));
        Event event = eventMapper.mapToEvent(newEventDto);
      /*  if(event.getEventDate().isAfter(LocalDateTime.now().minusHours(2))){
            throw new ValidateDataException("Время начала события должно быть хотя бы на 2 часа позже");
        }*/

        Location location = locationMapper.mapToLocation(newEventDto.getLocation());
        location = locationRepository.save(location);

        event.setState(EventState.PENDING);
        event.setLocation(location);
        event.setInitiator(user);
        event.setCreatedOn(LocalDateTime.now());
        event.setCategory(category);

        event = eventRepository.save(event);
        log.info("Успешное создание события {}",event);
        return eventMapper.mapToEventFullDto(event);
    }

    @Override
    public List<EventShortDto> getAllEventsPrivate(Long userId, int from, int size) {
        log.info("Получение всех событий пользователя с id {}",userId);
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id:"+userId+" не найден"));
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
    public EventFullDto getEventById(Long eventId, Long userId, HttpServletRequest httpServletRequest) {
        log.info("Получение события {}, пользователя с id {}",eventId,userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id:"+userId+" не найден"));
        Event event = eventRepository.findByIdAndInitiatorId(eventId,userId).orElseThrow(() -> new NotFoundException("Событие с id:"+eventId+" не найдено"));
        if(!event.getState().equals(EventState.PUBLISHED)){
            throw new NotFoundException("Событие с id:"+eventId+" не опубликовано");
        }

        statClient.create(httpServletRequest);

        EventFullDto eventFullDto = eventMapper.mapToEventFullDto(event);
        Map<Long, Long> viewStatsMap = getViewsAllEvents(List.of(event));
        Long views = viewStatsMap.getOrDefault(event.getId(), 0L);
        eventFullDto.setViews(Math.toIntExact(views));
        return eventFullDto;
    }

    @Transactional
    @Override
    public EventFullDto updateEventUser(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        log.info("Попытка обновить событие: {},{}",eventId,updateEventUserRequest);
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id:"+userId+" не найден"));
        Event event = eventRepository.findByIdAndInitiatorId(eventId,userId).orElseThrow(() -> new NotFoundException("Событие с id:"+eventId+" не найдено"));
       if(event.getState().equals(EventState.PUBLISHED)){
            throw new ValidateDataException("Опубликованные события нельзя изменить");
        }
        event = updateEventFromDto(updateEventUserRequest, event);
        event = eventRepository.save(event);
        event.setViews(getViews(event.getId(), event.getCreatedOn()));
        log.info("Успешное обновление события");
        return eventMapper.mapToEventFullDto(event);

    }

    @Transactional(readOnly = true)
    @Override
    public List<EventFullDto> getAllEventsAdmin(List<Long> users, List<String> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {

        List<EventState> eventStates = states.stream().map(EventState::valueOf).toList();

        Pageable pageable = PageRequest.of(from, size);

        return eventRepository.findAll(users,eventStates,categories,rangeStart,rangeEnd,pageable).stream().map(event -> {
                    CategoryDto categoryDto = categoryMapper.mapToCategoryDto(event.getCategory());
                    UserShortDto userShortDto = userMapper.toUserShortDto(event.getInitiator());
                    long views = getViews(event.getId(), event.getCreatedOn());

                    EventFullDto eventFullDto = eventMapper.mapToEventFullDto(event);
                    eventFullDto.setCategory(categoryDto);
                    eventFullDto.setInitiator(userShortDto);
                    eventFullDto.setViews((int) views);

                    return eventFullDto;
                })
                .toList();
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
    public EventRequestStatusUpdateResult updateStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest updateStatusRequest) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id:" + eventId + " не найдено"));

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id:"+userId+" не найден"));

        if(event.getParticipantLimit() == 0 || !event.getRequestModeration()){
            return new EventRequestStatusUpdateResult();
        }
        if(!event.getState().equals(EventState.PUBLISHED)){
            throw new ValidateDataException("Событие не опубликовано");
        }
        if(event.getConfirmedRequests() == event.getParticipantLimit()){
            throw new ConflictException("Места зкончились");
        }

        List<EventRequest> requests = eventRequestRepository.findAllByIdIn(updateStatusRequest.getRequestIds().stream().toList());
        validateRequestWithEvent(requests,event.getId());

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();


        switch (updateStatusRequest.getStatus()) {
            case REJECTED:
                rejectRequests(result, requests);
                break;
            case CONFIRMED:
                confirmRequests(result, event, requests);
                break;
            default:
                throw new IllegalArgumentException("Неизвестный статус для обновления");
        }
        return result;

    }

    private void confirmRequests(EventRequestStatusUpdateResult result, Event event, Collection<EventRequest> requests) {
        int limit = event.getParticipantLimit();
        int currentConfirmed = event.getConfirmedRequests();

        for (EventRequest request : requests) {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Можно подтвердить только заявки в статусе ожидания");
            }
            if (limit != 0 && currentConfirmed >= limit) {
                request.setStatus(RequestStatus.REJECTED);
                EventRequest updatedRequest = eventRequestRepository.save(request);
                result.getRejectedRequests().add(eventRequestMapper.toParticipationRequestDto(updatedRequest));

            } else {
                request.setStatus(RequestStatus.CONFIRMED);
                EventRequest updatedRequest = eventRequestRepository.save(request);
                result.getConfirmedRequests().add(eventRequestMapper.toParticipationRequestDto(updatedRequest));
                currentConfirmed++;
            }
        }
        event.setViews(currentConfirmed);
        eventRepository.save(event);
    }

    private void rejectRequests(EventRequestStatusUpdateResult result,List<EventRequest> requests) {
        for (EventRequest eventRequest : requests) {

            if(eventRequest.getStatus() != RequestStatus.PENDING){
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
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с id:"+eventId+" не найдено"));
        if(event.getState().equals(EventState.PUBLISHED)){
            throw new ValidateDataException("Опубликованные события нельзя изменить");
        }
        if(LocalDateTime.parse(updateEventAdminRequest.getEventDate(),DATE_TIME_FORMATTER).isAfter(LocalDateTime.now().minusHours(1))) {
            throw new ValidateDataException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
        }
        validateUpdateAdmin(event.getState(),updateEventAdminRequest.getState());
        event = updateEventFromAdminDto(updateEventAdminRequest,event);
        event = eventRepository.save(event);
        event.setViews(getViews(event.getId(), event.getCreatedOn()));
        return eventMapper.mapToEventFullDto(event);
    }

    @Override
    public List<EventShortDto> getAllEventPublicRequest(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, SortType sortType, int from, int size,HttpServletRequest httpServletRequest) {

        LocalDateTime start = rangeStart == null ? LocalDateTime.now() : rangeStart;
        LocalDateTime end = rangeEnd == null ? LocalDateTime.now().plusYears(1) : rangeEnd;

        List<Event> events = new ArrayList<>();

        statClient.create(httpServletRequest);

        switch (sortType){
            case EVENT_DATE:
                Pageable pageable = PageRequest.of(from, size, Sort.by("eventDate"));
                events = eventRepository.findAllPublicRequest(text,categories,paid,start,end,onlyAvailable,EventState.PUBLISHED,pageable);
                break;
            case VIEWS:
                Pageable pageableV = PageRequest.of(from, size, Sort.by("views"));
                events = eventRepository.findAllPublicRequest(text,categories,paid,start,end,onlyAvailable,EventState.PUBLISHED,pageableV);
        }

        Map<Long, Long> views = getViewsAllEvents(events);

        List<EventShortDto> eventShortDtos = events.stream()
                .map(event -> {
                    Long eventId = event.getId();
                    Long viewCount = views.getOrDefault(eventId, 0L); // Получаем просмотры или 0, если нет данных
                    EventShortDto eventShortDto = eventMapper.toEventShortDto(event);
                    eventShortDto.setViews(Math.toIntExact(viewCount));
                    return eventShortDto;
                })
                .toList();


        return eventShortDtos;
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getEventByIdPublic(Long eventId, HttpServletRequest httpServletRequest) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с id:"+eventId+" не найдено"));
        statClient.create(httpServletRequest);

        if(!event.getState().equals(EventState.PUBLISHED)){
            throw new ValidateDataException("Событие не опубликовано");
        }

        EventFullDto eventShortDto = eventMapper.mapToEventFullDto(event);
        eventShortDto.setViews(getViews(event.getId(), event.getCreatedOn()));
        return eventShortDto;
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
            List<ViewStats> viewStatsList = Arrays.asList(new ObjectMapper().convertValue(response.getBody(), ViewStats[].class));
            return viewStatsList.stream()
                    .collect(Collectors.toMap(
                            viewStats -> Long.parseLong(viewStats.getUri().substring("/events/".length())),
                            ViewStats::getHits
                    ));
        } else {
            log.warn("Не удалось получить статистику просмотров. Код ответа: {}", response.getStatusCodeValue());
            return Collections.emptyMap();
        }
    }

    private Event updateEventFromDto(UpdateEventUserRequest dto, Event event) {
        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getEventDate() != null) event.setEventDate(LocalDateTime.parse(dto.getEventDate()));
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
        return event;

    }

    private void validateRequestWithEvent(List<EventRequest> requests,Long eventId){
        for (EventRequest request : requests){
            if(!request.getRequesterId().equals(eventId)){
                throw new ConflictException("Запрос с id:"+request.getId()+" не относится к событию с id:"+eventId);
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
            event.setEventDate(LocalDateTime.parse(dto.getEventDate()));
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
        if (dto.getState() != null) {
            switch (dto.getState()) {
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

        if(eventStateAction.equals(EventStateAction.PUBLISH_EVENT) && !eventState.equals(EventState.PENDING)) {
            throw new ConflictException("событие можно публиковать, только если оно в состоянии ожидания публикации");
        }

        if(eventStateAction.equals(EventStateAction.REJECT_EVENT) && eventState.equals(EventState.PUBLISHED)) {
            throw new ConflictException("событие можно отклонить, только если оно еще не опубликовано");
        }



    }
}
