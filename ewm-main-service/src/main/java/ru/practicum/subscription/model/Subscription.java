package ru.practicum.subscription.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.subscription.enums.FriendShipStatus;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subscriptions")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "follower_id")
    private User follower;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "subscribe_time")
    private LocalDateTime subscribeTime;

    @Column(name = "unsubscribe_time")
    private LocalDateTime unsubscribeTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "friendships_status", nullable = false)
    private FriendShipStatus friendshipsStatus;
}