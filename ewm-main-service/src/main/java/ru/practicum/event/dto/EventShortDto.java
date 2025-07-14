package ru.practicum.event.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.user.dto.UserShortDto;

@Getter
@Setter
public class EventShortDto {

    @NotNull(message = "описание не может быть пустым")
    private String annotation;

    @NotNull(message = "Категория не может быть пустой")
    private CategoryDto category;

    private Integer confirmedRequests;

    @NotNull(message = "Дата события не может быть пустой")
    private String eventDate;

    private Long id;

    @NotNull(message = "Создатель события не может быть null")
    private UserShortDto initiator;


    @NotNull(message = "статус не может быть пустым")
    private Boolean paid;

    @NotNull(message = "Заголовок не может быть пустым")
    private String title;

    private Integer views;
}
