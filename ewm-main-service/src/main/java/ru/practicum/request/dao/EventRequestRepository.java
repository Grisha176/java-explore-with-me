package ru.practicum.request.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.request.enums.RequestStatus;
import ru.practicum.request.model.EventRequest;

import java.util.List;

public interface EventRequestRepository extends JpaRepository<EventRequest, Long> {

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

    List<EventRequest> findAllByRequesterId(Long requesterId);

    Long countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<EventRequest> findAllByEventId(Long eventId);

    List<EventRequest> findAllByIdIn(List<Long> ids);
}
