package ru.practicum.event.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.event.enums.EventState;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByIdAndInitiatorId(Long id, Long userId);

    @Query("SELECT e FROM Event e WHERE e.initiator.id IN :users " +
            "AND e.state IN :states " +
            "AND e.category.id IN :categories " +
            "AND e.eventDate BETWEEN :start AND :end")
    List<Event> findAll(@Param("users") List<Long> userIds,
                        @Param("states") List<EventState> states,
                        @Param("categories") List<Long> categories,
                        @Param("start") LocalDateTime rangeStart,
                        @Param("end") LocalDateTime rangeEnd,
                        Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "WHERE (LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "       OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "  AND e.category.id IN :categories " +
            "  AND e.paid = :paid " +
            "  AND e.eventDate BETWEEN :start AND :end " +
            "  AND (:onlyAvailable = false OR (e.participantLimit = 0 OR e.confirmedRequests < e.participantLimit))"+
             "AND e.state = :state")
    List<Event> findAllPublicRequest(@Param("text") String text,
                        @Param("categories") List<Long> categories,
                        @Param("paid") Boolean paid,
                        @Param("start") LocalDateTime rangeStart,
                        @Param("end") LocalDateTime rangeEnd,
                        @Param("onlyAvailable") Boolean onlyAvailable,
                        @Param("state") EventState state,
                        Pageable pageable);

    List<Event> findAllByIdIn(List<Long> ids);

}
