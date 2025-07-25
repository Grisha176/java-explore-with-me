package ru.practicum.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.subscription.dto.SubscriberData;
import ru.practicum.subscription.dto.SubscriptionDto;
import ru.practicum.subscription.model.Subscription;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    SubscriptionDto mapToSubscriptionDto(Subscription subscription);

    @Mapping(source = "follower.name",target = "ownerName")
    @Mapping(source = "follower.id",target = "userId")
    SubscriberData mapToSubscriberData(Subscription subscription);

}
