package ru.practicum.subscription.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.subscription.enums.FriendShipStatus;

import java.time.LocalDateTime;

@Getter
@Setter
public class SubscriberData {

    private Long userId;
    private String ownerName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime subscribeTime;
    private FriendShipStatus friendshipsStatus;
}
