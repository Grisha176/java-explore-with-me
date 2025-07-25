package ru.practicum.subscription.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.subscription.enums.FriendShipStatus;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@Getter
@Setter
public class SubscriptionDto {

    private Long id;
    private User follower;
    private User owner;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime subscribeTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime unsubscribeTime;
    private FriendShipStatus friendshipsStatus;
}
