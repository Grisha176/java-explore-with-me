package ru.practicum.event.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    List<Event> findAllByCategoryId(Long categoryId);


    @Query("""
                SELECT e
                FROM Event AS e
                WHERE (?1 IS NULL or e.initiator.id IN ?1)
                    AND (?2 IS NULL or e.state IN ?2)
                    AND (?3 IS NULL or e.category.id in ?3)
                    AND (CAST(?4 AS timestamp) IS NULL or e.eventDate >= ?4)
                    AND (CAST(?5 AS timestamp) IS NULL or e.eventDate < ?5)
            """)
    List<Event> findAll(
            List<Long> users,
            List<EventState> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Pageable pageable
    );


    @Query("SELECT e FROM Event e " +
            "WHERE (:text IS NULL OR " +
            "LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND (:start IS NULL OR :end IS NULL OR (e.eventDate >= :start AND e.eventDate <= :end)) " +
            "AND (:onlyAvailable IS NULL OR :onlyAvailable = false OR (e.participantLimit = 0 OR e.confirmedRequests < e.participantLimit)) " +
            "AND (:state IS NULL OR e.state = :state)")
    List<Event> findAllPublicRequest(@Param("text") String text,
                                     @Param("categories") List<Long> categories,
                                     @Param("paid") Boolean paid,
                                     @Param("start") LocalDateTime rangeStart,
                                     @Param("end") LocalDateTime rangeEnd,
                                     @Param("onlyAvailable") Boolean onlyAvailable,
                                     @Param("state") EventState state,
                                     Pageable pageable);

    Page<Event> findAll(Specification<Event> spec, Pageable pageable);

    List<Event> findAllByIdIn(List<Long> ids);

    List<Event> findByInitiatorIdIn(List<Long> initiatorId, Pageable pageable);

}
