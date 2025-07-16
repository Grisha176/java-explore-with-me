package ru.practicum.event.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.enums.EventState;
import ru.practicum.location.Location;
import ru.practicum.location.LocationDto;
import ru.practicum.user.dto.UserShortDto;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventFullDto {

    @NotNull(message = "описание не может быть пустым")
    private String annotation;

    @NotNull(message = "Категория не может быть пустой")
    private CategoryDto category;

    private Integer confirmedRequests;

    private String createdOn;

    private String description;

    @NotNull(message = "Дата события не может быть пустой")
    private String eventDate;

    private Long id;

    @NotNull(message = "Создатель события не может быть null")
    private UserShortDto initiator;

    @NotNull(message = "локация не может быть пустой")
    private LocationDto    location;

    @NotNull(message = "статус не может быть пустым")
    private Boolean paid;

    private Integer participantLimit = 0;

    private String publishedOn;

    private Boolean requestModeration = true;

    private EventState state;

    @NotNull(message = "Заголовок не может быть пустым")
    private String title;

    private Integer views;

}
