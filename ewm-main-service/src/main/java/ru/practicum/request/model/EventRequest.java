package ru.practicum.request.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.event.model.Event;
import ru.practicum.request.enums.RequestStatus;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "requests")
public class EventRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    Event event;

    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    User requester;

    @Enumerated(EnumType.STRING)
    RequestStatus status;
    LocalDateTime created;
}
