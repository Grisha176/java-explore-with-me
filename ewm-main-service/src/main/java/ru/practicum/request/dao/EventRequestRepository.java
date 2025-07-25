package ru.practicum.request.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.request.enums.RequestStatus;
import ru.practicum.request.model.EventRequest;

import java.util.List;

public interface EventRequestRepository extends JpaRepository<EventRequest, Long> {

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

    List<EventRequest> findAllByRequesterId(Long requesterId);

    Long countByEventIdAndStatus(Long eventId, RequestStatus status);

    long countByEventId(Long eventId);

    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    List<EventRequest> findAllByEventId(Long eventId);

    List<EventRequest> findAllByIdIn(List<Long> ids);


    @Query("SELECT r.event.id,COUNT(r) FROM EventRequest r " +
            "WHERE r.event.id IN :eventIds AND r.status = :status " +
            "GROUP BY r.event.id")
    List<Object[]> countByEventIdInAndStatus(@Param("eventIds") List<Long> eventIds, @Param("status") RequestStatus status);
}
