package ru.practicum.subscription.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.event.dao.EventRepository;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.Event;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mappers.EventMapper;
import ru.practicum.mappers.SubscriptionMapper;
import ru.practicum.subscription.dao.SubscriptionRepository;
import ru.practicum.subscription.dto.SubscriberData;
import ru.practicum.subscription.dto.SubscriptionDto;
import ru.practicum.subscription.enums.FriendShipStatus;
import ru.practicum.subscription.model.Subscription;
import ru.practicum.user.dao.UserRepository;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;


    @Transactional
    @Override
    public SubscriptionDto subscribe(Long ownerId, Long followerId) {
        User owner = userRepository.findById(ownerId).orElseThrow(() -> new NotFoundException("Пользователь с id:" + ownerId + " не найден"));
        User follower = userRepository.findById(followerId).orElseThrow(() -> new NotFoundException("Пользователь с id:" + followerId + " не найден"));

        validateSubscription(owner, follower);

        Subscription subscription = createOrUpdateSubscribe(owner, follower);
        log.info("Подписка успешно создана для пользователя {} на пользователя {}. Статус дружбы: {}",
                followerId, ownerId, subscription.getFriendshipsStatus());
        log.info("sub" + subscriptionRepository.findAll());

        return subscriptionMapper.mapToSubscriptionDto(subscription);
    }


    @Override
    public void unsubscribe(Long userId, Long ownerId) {
        User follower = findUser(userId);
        User owner = findUser(ownerId);
        log.info("Обработка запроса на отмену подписки от пользователя {} к пользователю {}", userId, ownerId);

        Optional<Subscription> subscription = subscriptionRepository
                .findByFollowerAndOwner(follower, owner);
        if (subscription.isEmpty()) {
            log.warn("У пользователя {} нет подписки на пользователя {}", userId, ownerId);
            throw new ConflictException("У пользователя нет подписки на пользователя");
        }
        Optional<Subscription> reverseSubscription = subscriptionRepository
                .findByFollowerAndOwner(owner, follower);
        if (reverseSubscription.isPresent() &&
                reverseSubscription.get().getFriendshipsStatus().equals(FriendShipStatus.MUTUAL)) {
            reverseSubscription.get().setFriendshipsStatus(FriendShipStatus.ONE_SIDED);
            reverseSubscription.get().setUnsubscribeTime(LocalDateTime.now());
            subscriptionRepository.save(reverseSubscription.get());
        }
        subscriptionRepository.delete(subscription.get());
        log.info("Пользователь {} успешно отписался от {}. Статус дружбы обновлен: {}",
                userId, ownerId, reverseSubscription.map(Subscription::getFriendshipsStatus).orElse(null));
    }

    @Override
    public List<EventShortDto> getEventsFromSubscriptions(Long userId, int from, int size) {
        User follower = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        List<Subscription> subscriptions = subscriptionRepository.findByFollower(follower);

        List<Long> ownerIds = subscriptions.stream()
                .map(subscription -> subscription.getOwner().getId())
                .collect(Collectors.toList());

        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by("eventDate").descending());
        List<Event> events = eventRepository.findByInitiatorIdIn(ownerIds, pageRequest);

        return events.stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public Long getSubscriberCount(Long userId) {
        log.info("Получение количества подписчиков для пользователя с ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        long count = subscriptionRepository.countByOwnerAndFriendshipsStatusIn(user, List.of(FriendShipStatus.ONE_SIDED, FriendShipStatus.MUTUAL));
        log.info("У пользователя {} {} подписчиков.", userId, count);
        return count;
    }

    @Override
    public List<SubscriberData> getAllSubscribers(Long userId, int from, int size) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        Pageable pageable = PageRequest.of(from / size, size);
        List<Subscription> subscriptions = subscriptionRepository.findByOwner(owner, pageable);
        return subscriptions.stream()
                .map(subscriptionMapper::mapToSubscriberData)
                .collect(Collectors.toList());
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь c ID " + userId + " не найден"));
    }

    private void validateSubscription(User follower, User owner) {

        log.info("su" + follower.isAllowSubscriptions());
        if (!follower.isAllowSubscriptions()) {
            throw new ConflictException("Подписки на пользователя не разрешены");
        }

        Optional<Subscription> subscription = subscriptionRepository.findByFollowerAndOwner(follower, owner);

        if (subscription.isPresent()) {
            Subscription sub = subscription.get();
            if (sub.getFriendshipsStatus() == FriendShipStatus.ONE_SIDED || sub.getFriendshipsStatus() == FriendShipStatus.MUTUAL) {
                log.warn("У пользователя {} уже есть подписка на пользователя {}", follower.getId(), owner.getId());
                throw new ConflictException("У пользователя уже есть подписка на пользователя");
            }
        }
    }

    private Subscription createOrUpdateSubscribe(User owner, User follower) {
        Subscription subscription = new Subscription();
        subscription.setOwner(owner);
        subscription.setFollower(follower);

        Optional<Subscription> existsSubscription = subscriptionRepository.findByFollowerAndOwner(owner, follower);

        if (existsSubscription.isPresent()) {
            subscription.setFriendshipsStatus(FriendShipStatus.MUTUAL);
            subscription.setSubscribeTime(LocalDateTime.now());

            existsSubscription.get().setSubscribeTime(LocalDateTime.now());
            existsSubscription.get().setFriendshipsStatus(FriendShipStatus.MUTUAL);
            subscriptionRepository.saveAll(List.of(subscription, existsSubscription.get()));
        } else {
            subscription.setFriendshipsStatus(FriendShipStatus.ONE_SIDED);
            subscription.setSubscribeTime(LocalDateTime.now());
            subscriptionRepository.save(subscription);
        }
        return subscription;
    }
}
