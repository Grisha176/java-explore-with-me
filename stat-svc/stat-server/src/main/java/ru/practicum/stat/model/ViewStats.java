package ru.practicum.stat.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ViewStats {

    private String app;

    private String uri;

    private Long hits;
}
