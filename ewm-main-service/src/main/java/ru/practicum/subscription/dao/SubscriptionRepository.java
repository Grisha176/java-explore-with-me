package ru.practicum.subscription.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.subscription.enums.FriendShipStatus;
import ru.practicum.subscription.model.Subscription;
import ru.practicum.user.model.User;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByFollowerAndOwner(User follower, User owner);

    List<Subscription> findByFollower(User follower);

    List<Subscription> findByOwner(User owner, Pageable pageable);

    long countByOwnerAndFriendshipsStatusIn(User owner, List<FriendShipStatus> subscriptions);
}
