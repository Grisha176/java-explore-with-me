package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.event.enums.EventStateActionUserRequest;
import ru.practicum.location.Location;

import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventUserRequest {


    @Size(min = 20, max = 2000, message = "Длина аннотации должна не больше 2000 символов и не меньше 20")
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000, message = "Длина описания должна не больше 7000 символов и не меньше 20")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private Location location;

    private Boolean paid;

    @Min(value = 0, message = "Количество участников не может быть отрицательным числом")
    private Integer participantLimit;

    private Boolean requestModeration;

    private EventStateActionUserRequest stateAction;

    @Size(min = 3, max = 120, message = "Размер заголовка может быть от 3 до 120 сиволов")
    private String title;
}
