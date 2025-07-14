package ru.practicum.event.dto;

import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.event.enums.EventStateAction;
import ru.practicum.location.Location;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventAdminRequest {

    @Size(min = 20, max = 2000, message = "Длина аннотации должна не больше 2000 символов и не меньше 20")
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000, message = "Длина описания должна не больше 7000 символов и не меньше 20")
    private String description;

    private String eventDate;

    private Location location;

    private Boolean paid;

    private Integer participantLimit;

    private Boolean requestModeration = true;

    private EventStateAction state;

    @Size(min = 3,max = 120,message = "Размер заголовка может быть от 3 до 120 сиволов")
    private String title;

}
