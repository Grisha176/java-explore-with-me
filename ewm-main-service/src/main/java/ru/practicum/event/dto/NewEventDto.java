package ru.practicum.event.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.location.LocationDto;

@Getter
@Setter
public class NewEventDto {

    @NotNull(message = "описание не может быть пустым")
    @Size(min = 20, max = 2000, message = "Длина аннотации должна не больше 2000 символов и не меньше 20")
    private String annotation;

    @NotNull(message = "Категория не может быть пустой")
    private Long category;

    @NotNull(message = "описание не может быть пустым")
    @Size(min = 20, max = 7000, message = "Длина описания должна не больше 7000 символов и не меньше 20")
    private String description;

    @NotNull(message = "Дата события не может быть пустой")
    private String eventDate;


    @NotNull(message = "локация не может быть пустой")
    private LocationDto location;

    private Boolean paid = false;

    private Integer participantLimit = 0;

    private Boolean requestModeration = true;

    @NotNull(message = "Заголовок не может быть пустым")
    @Size(min = 3, max = 120, message = "Длина названия должна не больше 120 символов и не меньше 3")
    private String title;

}
