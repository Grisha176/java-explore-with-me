package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.enums.EventState;
import ru.practicum.location.LocationDto;
import ru.practicum.user.dto.UserShortDto;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventFullDto {


    private Long id;

    @NotNull(message = "Заголовок не может быть пустым")
    private String title;

    @NotNull(message = "описание не может быть пустым")
    private String annotation;
    private String description;

    @NotNull(message = "Дата события не может быть пустой")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String eventDate;

    @NotNull(message = "локация не может быть пустой")
    private LocationDto location;

    @NotNull(message = "статус не может быть пустым")
    private Boolean paid;

    private Integer participantLimit = 0;
    private Boolean requestModeration;
    private EventState state;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String createdOn;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String publishedOn;

    @NotNull(message = "Создатель события не может быть null")
    private UserShortDto initiator;

    @NotNull(message = "Категория не может быть пустой")
    private CategoryDto category;

    private Integer confirmedRequests;

    private Integer views;

}
