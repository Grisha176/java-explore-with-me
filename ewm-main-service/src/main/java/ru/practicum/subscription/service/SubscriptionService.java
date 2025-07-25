package ru.practicum.subscription.service;

import ru.practicum.event.dto.EventShortDto;
import ru.practicum.subscription.dto.SubscriberData;
import ru.practicum.subscription.dto.SubscriptionDto;

import java.util.List;

public interface SubscriptionService {

    SubscriptionDto subscribe(Long ownerId, Long followerId);

    void unsubscribe(Long userId, Long ownerId);

    List<EventShortDto> getEventsFromSubscriptions(Long userId, int from, int size);

    Long getSubscriberCount(Long userId);

    List<SubscriberData> getAllSubscribers(Long userId, int from, int size);

}
