package ru.practicum.location;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LocationDto {

    private Long id;

    private double lat;

    private double lon;
}
