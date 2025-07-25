package ru.practicum.compilation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.event.model.Event;

import java.util.List;

@Table(name = "compilations")
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean pinned;

    @Column(nullable = false)
    @Size(min = 1, max = 50,message = "Длина названия должна быть от 1 до 50 символов")
    private String title;


    @ManyToMany
    @JoinTable(name = "compilations_event",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id"))
    private List<Event> events;




}
