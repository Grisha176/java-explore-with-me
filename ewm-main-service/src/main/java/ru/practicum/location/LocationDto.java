package ru.practicum.location;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationDto {

    private Long id;

    private double lat;

    private double lon;
}
